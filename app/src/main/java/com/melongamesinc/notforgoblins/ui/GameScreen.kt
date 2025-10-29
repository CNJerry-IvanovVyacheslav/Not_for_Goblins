package com.melongamesinc.notforgoblins.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.melongamesinc.notforgoblins.data.GameState
import com.melongamesinc.notforgoblins.domain.models.Card
import com.melongamesinc.notforgoblins.domain.models.CardEffectType
import kotlinx.coroutines.delay
import kotlin.math.pow

@Composable
fun GameScreen() {
    val gameState = remember { GameState() }
    var frameTick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            if (gameState.running) {
                gameState.update(16f)
                frameTick++
            }
            delay(16L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFe6f7d6))
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🏰 Not for Goblins", modifier = Modifier.padding(8.dp))
            Text("Wave: ${gameState.waveNumber}", modifier = Modifier.padding(8.dp))
            Text("💰 ${gameState.gold}", modifier = Modifier.padding(8.dp))
            Text("❤️ ${gameState.baseHealth}", modifier = Modifier.padding(8.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            GameCanvas(gameState, frameTick)

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
            horizontalArrangement = Arrangement.Center
        ) {
            when {
                gameState.placingStartingTower -> {
                    Text("Tap a green slot to place your starting tower", color = Color.Black)
                }
                gameState.awaitingTowerPlacement -> {
                    Text("Tap a green slot to place your new tower", color = Color.Black)
                }
                !gameState.running && !gameState.showCardChoice && gameState.baseHealth > 0 -> {
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
fun GameCanvas(gameState: GameState, frameTick: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canPlace = gameState.placingStartingTower || gameState.awaitingTowerPlacement
                    if (canPlace) {
                        gameState.towerSlots.forEachIndexed { index, (sx, sy) ->
                            if ((sx - offset.x).pow(2) + (sy - offset.y).pow(2) < 2500f) {
                                if (gameState.isSlotFree(index)) {
                                    gameState.placeTower(index)
                                }
                            }
                        }
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
            drawRect(
                color = Color(0xFF4b8b3b),
                topLeft = Offset(t.x - 25f, t.y - 25f),
                size = Size(50f, 50f)
            )
        }

        gameState.enemies.forEach { e ->
            drawCircle(color = Color(0xFFb22222), radius = 20f, center = Offset(e.x, e.y))
        }

        gameState.projectiles.forEach { p ->
            drawCircle(color = Color.Yellow, radius = 6f, center = Offset(p.x, p.y))
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
                Button(onClick = {
                    gameState.applyCard(card)
                }) {
                    Text("${card.title}: ${card.description}")
                }
            }
        }
    }
}