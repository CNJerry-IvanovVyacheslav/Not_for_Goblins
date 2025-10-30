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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.melongamesinc.notforgoblins.data.GameState
import com.melongamesinc.notforgoblins.domain.models.FastGoblin
import com.melongamesinc.notforgoblins.domain.models.SlowTower
import com.melongamesinc.notforgoblins.domain.models.SniperTower
import com.melongamesinc.notforgoblins.domain.models.SplashTower
import com.melongamesinc.notforgoblins.domain.models.TankGoblin
import kotlin.math.pow

@Composable
fun GameScreen() {
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
        TopBar(gameState)
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
                    },
                    enabled = gameState.gold >= cost
                ) { Text("Upgrade") }

                Button(onClick = { gameState.cancelUpgrade() }) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun GameCanvas(gameState: GameState, frameTick: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (gameState.showCardChoice ||
                        gameState.showUpgradeModal ||
                        (gameState.awaitingTowerPlacement && gameState.towerToPlaceOptions.isNotEmpty())
                    ) {
                        return@detectTapGestures
                    }

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
                    } else {
                        gameState.upgradeTowerAt(offset.x, offset.y)
                    }
                }
            }
    ) {
        val path = gameState.path
        for (i in 0 until path.size - 1) {
            val (x1, y1) = path[i]
            val (x2, y2) = path[i + 1]
            drawLine(
                color = Color(0xFFa58b63),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 40f
            )
        }

        gameState.towerSlots.forEachIndexed { index, (sx, sy) ->
            val isFree = gameState.isSlotFree(index)
            val canPlace = gameState.placingStartingTower || gameState.awaitingTowerPlacement
            val color = when {
                isFree && canPlace -> Color(0x7700FF00)
                isFree -> Color(0x33FFFFFF)
                else -> Color(0xFF4b8b3b)
            }
            drawRect(color, topLeft = Offset(sx - 25f, sy - 25f), size = Size(50f, 50f))
        }

        gameState.towers.forEach { t ->
            val color = when (t) {
                is SplashTower -> Color(0xFF7B3F00)
                is SniperTower -> Color(0xFF2E86AB)
                is SlowTower -> Color(0xFF7BDFF6)
                else -> Color(0xFF4b8b3b)
            }
            drawRect(color, topLeft = Offset(t.x - 25f, t.y - 25f), size = Size(50f, 50f))
        }

        gameState.enemies.forEach { e ->
            val col = when (e) {
                is TankGoblin -> Color(0xFF8B0000)
                is FastGoblin -> Color(0xFFFFA500)
                else -> Color(0xFFb22222)
            }
            drawCircle(color = col, radius = 20f, center = Offset(e.x, e.y))
        }

        gameState.projectiles.forEach { p ->
            drawCircle(color = Color.Yellow, radius = 6f, center = Offset(p.x, p.y))
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
                    },
                    modifier = Modifier
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
fun TopBar(gameState: GameState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF4CAF50),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(gameState.mapName, color = Color.White)
            Text("Wave: ${gameState.waveNumber}", color = Color.White)
            Text("⏳ ${"%.1f".format(gameState.timeToNextWave)}s", color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
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