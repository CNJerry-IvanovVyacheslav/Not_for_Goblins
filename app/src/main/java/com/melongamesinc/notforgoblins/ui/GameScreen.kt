package com.melongamesinc.notforgoblins.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.melongamesinc.notforgoblins.data.GameState
import com.melongamesinc.notforgoblins.domain.models.BasicGoblin
import com.melongamesinc.notforgoblins.domain.models.FastGoblin
import com.melongamesinc.notforgoblins.domain.models.SlowProjectile
import com.melongamesinc.notforgoblins.domain.models.SlowTower
import com.melongamesinc.notforgoblins.domain.models.SniperTower
import com.melongamesinc.notforgoblins.domain.models.SplashProjectile
import com.melongamesinc.notforgoblins.domain.models.SplashTower
import com.melongamesinc.notforgoblins.domain.models.TankGoblin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun GameScreen(onBackToMenu: () -> Unit) {
    val gameState = remember { GameState() }
    var frameTick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        var lastFrameTime = System.currentTimeMillis()
        while (true) {
            val now = System.currentTimeMillis()
            val delta = (now - lastFrameTime).toFloat()
            lastFrameTime = now

            if (gameState.running) {
                gameState.update(delta, now)
                frameTick++
            }
            kotlinx.coroutines.delay(16L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFe6f7d6))
    ) {
        TopBar(gameState, onBackToMenu)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            GameCanvas(gameState, frameTick)

            if (gameState.awaitingTowerPlacement && gameState.towerToPlaceOptions.isNotEmpty()) {
                TowerChoiceModal(gameState)
            }

            if (gameState.showUpgradeModal) {
                UpgradeTowerModal(gameState)
            }

            if (gameState.showCardChoice) {
                CardSelectionModal(gameState)
            }

            if (gameState.baseHealth <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💀 Game Over 💀", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { gameState.startGame() }) {
                            Text("Restart")
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                gameState.placingStartingTower -> {
                    Text("Tap a green slot to place your starting tower", color = Color.Black)
                }

                gameState.awaitingTowerPlacement -> {
                    Text("Tap a green slot to place your new tower", color = Color.Black)
                }

                !gameState.running && !gameState.showCardChoice && !gameState.showUpgradeModal && gameState.baseHealth > 0 -> {
                    Button(onClick = { gameState.startGame() }) {
                        Text("Start")
                    }
                }

                gameState.running -> {
                    Button(onClick = { gameState.running = false }) {
                        Text("Pause")
                    }
                }
            }
        }
    }
}


@Composable
fun UpgradeTowerModal(gameState: GameState) {
    val tower = gameState.towerToUpgrade ?: return

    val nextDamage = tower.damage + (3 * 1.05.pow(tower.level)).toInt()
    val nextRange = tower.range + 20f * 1.02.pow(tower.level).toFloat()
    val nextFireRate = tower.fireRate * 1.05f
    val cost = gameState.upgradeCost(tower)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text("🔧 Upgrade Tower", color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Level: ${tower.level} → ${tower.level + 1}", color = Color.Black)
            Text("Damage: ${tower.damage} → $nextDamage", color = Color.Black)
            Text("Range: ${tower.range} → ${"%.1f".format(nextRange)}", color = Color.Black)
            Text(
                "Fire Rate: ${"%.2f".format(tower.fireRate)} → ${"%.2f".format(nextFireRate)}",
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Upgrade Cost: $cost 💰",
                color = if (gameState.gold >= cost) Color.Black else Color.Red
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (gameState.gold >= cost) gameState.upgradeTower(tower)
                    }, enabled = gameState.gold >= cost
                ) { Text("Upgrade") }

                Button(onClick = { gameState.cancelUpgrade() }) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun GameCanvas(gameState: GameState, frameTick: Int) {
    val flicker = remember { mutableStateOf(1f) }
    val flagWave = remember { mutableStateOf(0f) }
    val towerAngles = remember { mutableMapOf<Int, Float>() }
    val blinkProgress = remember { mutableStateOf(0f) }
    val blinkTimers =
        remember { mutableStateMapOf<Any, Float>() } // таймер моргания для каждого гоблина

    LaunchedEffect(frameTick) {
        blinkProgress.value = (sin(frameTick / 10f) + 1f) / 2f // 0..1, управляет морганием
    }


    // 🔥 Эффекты анимации факелов и флага
    LaunchedEffect(frameTick) {
        flicker.value = 0.8f + (0.2f * sin(frameTick / 5f))
        flagWave.value = sin(frameTick / 10f)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (gameState.showCardChoice || gameState.showUpgradeModal || (gameState.awaitingTowerPlacement && gameState.towerToPlaceOptions.isNotEmpty()))
                        return@detectTapGestures

                    val canPlace =
                        gameState.placingStartingTower || gameState.awaitingTowerPlacement
                    if (canPlace) {
                        gameState.towerSlots.forEachIndexed { index, (sx, sy) ->
                            if ((sx - offset.x).pow(2) + (sy - offset.y).pow(2) < 2500f) {
                                if (gameState.isSlotFree(index)) {
                                    val type = gameState.towerToPlace ?: "BASIC"
                                    gameState.placeTower(index, type)
                                }
                            }
                        }
                    } else gameState.upgradeTowerAt(offset.x, offset.y)
                }
            }
    ) {
        // 🌄 Фон
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFa8e063), Color(0xFF56ab2f))),
            size = size
        )

        val path = gameState.path

// 1️⃣ — Мягкая тень под дорогой (единый контур)
        drawPath(
            path = Path().apply {
                moveTo(path.first().first, path.first().second + 6f)
                for (i in 1 until path.size) {
                    val (x, y) = path[i]
                    lineTo(x, y + 6f)
                }
            },
            color = Color(0x22000000),
            style = Stroke(width = 60f, cap = StrokeCap.Round)
        )

// 2️⃣ — Основная дорога с закруглениями (единый контур)
        drawPath(
            path = Path().apply {
                moveTo(path.first().first, path.first().second)
                for (i in 1 until path.size) {
                    val (x, y) = path[i]
                    lineTo(x, y)
                }
            },
            color = Color(0xFFa58b63),
            style = Stroke(width = 40f, cap = StrokeCap.Round)
        )

        // 🏰 Замок дварфов
        val (castleX, castleY) = path.last()
        drawRect(
            color = Color(0xFF555555),
            topLeft = Offset(castleX - 60f, castleY - 60f),
            size = Size(120f, 120f)
        )

        // Башенки
        drawCircle(Color(0xFF3A3A3A), 20f, Offset(castleX - 50f, castleY - 50f))
        drawCircle(Color(0xFF3A3A3A), 20f, Offset(castleX + 50f, castleY - 50f))

        // Ворота
        drawRect(Color(0xFF2E2E2E), Offset(castleX - 20f, castleY + 10f), Size(40f, 50f))

        // 🔥 Факелы у входа (мерцают)
        val torchColor = Color(0xFFFFA000).copy(alpha = 0.6f + 0.4f * flicker.value)
        drawCircle(torchColor, 8f, Offset(castleX - 40f, castleY + 15f))
        drawCircle(torchColor, 8f, Offset(castleX + 40f, castleY + 15f))

        // 🚩 Флаг дварфов
        val flagX = castleX
        val flagY = castleY - 60f
        drawLine(Color.DarkGray, Offset(flagX, flagY), Offset(flagX, flagY - 40f), 4f)
        val flagPoints = listOf(
            Offset(flagX, flagY - 40f),
            Offset(flagX + 30f + 5f * flagWave.value, flagY - 30f),
            Offset(flagX + 30f + 5f * flagWave.value, flagY - 20f),
            Offset(flagX, flagY - 20f)
        )
        drawPath(
            path = Path().apply {
                moveTo(flagPoints[0].x, flagPoints[0].y)
                for (p in flagPoints.drop(1)) lineTo(p.x, p.y)
                close()
            },
            color = Color(0xFFB71C1C)
        )

        // 🏗️ Слоты под башни
        gameState.towerSlots.forEachIndexed { index, (sx, sy) ->
            val isFree = gameState.isSlotFree(index)
            val canPlace = gameState.placingStartingTower || gameState.awaitingTowerPlacement
            val color = when {
                isFree && canPlace -> Color(0x8800FF00)
                isFree -> Color(0x33000000)
                else -> Color.Transparent
            }
            drawCircle(color, 28f, Offset(sx, sy))
            if (!isFree) drawCircle(Color(0x22000000), 30f, Offset(sx + 3, sy + 3))
        }

        // 🏰 Башни с плавной наводкой
        gameState.towers.forEachIndexed { index, t ->
            val (baseColor, accentColor) = when (t) {
                is SplashTower -> Color(0xFF8B4513) to Color(0xFFFFA726)
                is SniperTower -> Color(0xFF546E7A) to Color(0xFF29B6F6)
                is SlowTower -> Color(0xFF4FC3F7) to Color(0xFF81D4FA)
                else -> Color(0xFF6D4C41) to Color(0xFFD7CCC8)
            }

            drawCircle(Color(0x33000000), 28f, Offset(t.x + 4, t.y + 4))
            drawCircle(baseColor, 25f, Offset(t.x, t.y))

            // 🔫 Плавное наведение пушки
            val nearestEnemy =
                gameState.enemies.minByOrNull { (it.x - t.x).pow(2) + (it.y - t.y).pow(2) }
            val targetAngle = if (nearestEnemy != null) {
                kotlin.math.atan2(nearestEnemy.y - t.y, nearestEnemy.x - t.x)
            } else 0f

            val currentAngle = towerAngles[index] ?: 0f
            val delta =
                ((targetAngle - currentAngle + Math.PI) % (2 * Math.PI) - Math.PI).toFloat() // кратчайший путь
            val newAngle = currentAngle + delta * 0.1f // 0.1f — скорость поворота
            towerAngles[index] = newAngle

            val gunLength = 20f
            val gunX = t.x + gunLength * kotlin.math.cos(newAngle)
            val gunY = t.y + gunLength * kotlin.math.sin(newAngle)
            drawLine(accentColor, Offset(t.x, t.y), Offset(gunX, gunY), 6f, cap = StrokeCap.Round)
        }


        // 👾 Враги с лицами и анимацией
        gameState.enemies.forEach { e ->
            // Определяем цвет головы гоблина
            val col = when (e) {
                is TankGoblin -> Color(0xFF093B00) // зелёный с красноватым оттенком
                is FastGoblin -> Color(0xFF8AFF37) // зелёный с оранжевым оттенком
                else -> Color(0xFF4CAF50)          // обычный зелёный
            }

            // Тень и голова
            drawCircle(Color(0x33000000), 20f, Offset(e.x + 3, e.y + 3))
            drawCircle(col, 18f, Offset(e.x, e.y))

            val (targetX, targetY) = e.nextTarget()
            val angle = atan2(targetY - e.y, targetX - e.x)

            // 👀 Глаза с морганием
            val blinkTime = blinkTimers.getOrElse(e) { (0..3000).random().toFloat() }
            blinkTimers[e] = blinkTime + 16f
            val isBlinking = blinkTime > 3000f
            val eyeHeight = when (e) {
                is BasicGoblin -> if (isBlinking) 0.5f else 3f
                is FastGoblin -> if (isBlinking) 0.5f else 2f
                is TankGoblin -> if (isBlinking) 0.5f else 2f
                else -> if (isBlinking) 0.5f else 3f
            }
            drawOval(
                Color.White,
                Offset(e.x - 5 - 2f, e.y - 6 - eyeHeight / 2),
                Size(4f, eyeHeight)
            )
            drawOval(
                Color.White,
                Offset(e.x + 5 - 2f, e.y - 6 - eyeHeight / 2),
                Size(4f, eyeHeight)
            )
            if (isBlinking) blinkTimers[e] = 0f

            // 👃 Нос
            val nosePath = Path().apply {
                when (e) {
                    is BasicGoblin -> {
                        val noseLength = 8f;
                        val noseWidth = 2f
                        moveTo(e.x + noseLength * cos(angle), e.y + noseLength * sin(angle))
                        lineTo(e.x - noseWidth * sin(angle), e.y + noseWidth * cos(angle))
                        lineTo(e.x + noseWidth * sin(angle), e.y - noseWidth * cos(angle))
                    }

                    is FastGoblin -> {
                        val noseLength = 6f;
                        val noseWidth = 1f
                        moveTo(
                            e.x + noseLength * cos(angle) - 2f * sin(angle),
                            e.y + noseLength * sin(angle) + 2f * cos(angle)
                        )
                        lineTo(e.x - noseWidth * sin(angle), e.y + noseWidth * cos(angle))
                        lineTo(e.x + noseWidth * sin(angle), e.y - noseWidth * cos(angle))
                    }

                    is TankGoblin -> {
                        val noseLength = 5f;
                        val noseWidth = 3f
                        moveTo(e.x + noseLength * cos(angle), e.y + noseLength * sin(angle))
                        lineTo(e.x - noseWidth * sin(angle), e.y + noseWidth * cos(angle))
                        lineTo(e.x + noseWidth * sin(angle), e.y - noseWidth * cos(angle))
                    }
                }
                close()
            }
            drawPath(nosePath, Color.Black)

            // 👄 Рот с колебанием
            val mouthOffset = when (e) {
                is BasicGoblin -> 1f * sin(frameTick / 5f)
                is FastGoblin -> 0f
                is TankGoblin -> -1f * sin(frameTick / 10f)
                else -> 0f
            }
            val mouthY = e.y + 7f + mouthOffset

            when (e) {
                is BasicGoblin -> { // улыбающийся
                    drawLine(Color.Black, Offset(e.x - 4, mouthY), Offset(e.x, mouthY + 2), 1.5f)
                    drawLine(Color.Black, Offset(e.x, mouthY + 2), Offset(e.x + 4, mouthY), 1.5f)
                }

                is FastGoblin -> { // прямой
                    drawLine(Color.Black, Offset(e.x - 4, mouthY), Offset(e.x + 4, mouthY), 1.5f)
                }

                is TankGoblin -> { // злой
                    drawLine(Color.Black, Offset(e.x - 4, mouthY + 2), Offset(e.x, mouthY), 1.5f)
                    drawLine(Color.Black, Offset(e.x, mouthY), Offset(e.x + 4, mouthY + 2), 1.5f)
                }

                else -> {
                    drawLine(Color.Black, Offset(e.x - 4, mouthY), Offset(e.x + 4, mouthY), 1.5f)
                }
            }
        }


        // 💥 Снаряды
        gameState.projectiles.forEach { p ->
            val col = when (p) {
                is SplashProjectile -> Color(0xFFFF7043)
                is SlowProjectile -> Color(0xFF80DEEA)
                else -> Color.Yellow
            }
            drawCircle(col, 6f, Offset(p.x, p.y))
        }
    }
}


@Composable
fun TowerChoiceModal(gameState: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text("🏰 Choose a Tower", color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            gameState.towerToPlaceOptions.forEach { type ->
                Button(
                    onClick = {
                        gameState.towerToPlace = type
                        gameState.towerToPlaceOptions = emptyList()
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(type)
                }
            }
        }
    }
}

@Composable
fun TopBar(
    gameState: GameState, onBackToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF4CAF50),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая часть
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(gameState.mapName, color = Color.White)
                Text("Wave: ${gameState.waveNumber}", color = Color.White)
                Text("⏳ ${"%.1f".format(gameState.timeToNextWave)}s", color = Color.White)
            }

            // Правая часть — иконка домой
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Color(0xFF388E3C), shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onBackToMenu() })
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Lvl ${gameState.level} (${gameState.experience}/${gameState.nextLevelXp})",
                color = Color.White
            )
            Text("💰 ${gameState.gold}", color = Color.White)
            Text("❤️ ${gameState.baseHealth}", color = Color.White)
        }
    }
}

@Composable
fun CardSelectionModal(gameState: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text("🎴 Choose Your Reward", color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            gameState.cardChoices.forEach { card ->
                Button(onClick = { gameState.applyCard(card) }) {
                    Text("${card.title}: ${card.description}")
                }
            }
        }
    }
}