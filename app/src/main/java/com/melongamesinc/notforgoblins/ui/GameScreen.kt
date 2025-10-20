package com.melongamesinc.notforgoblins.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.melongamesinc.notforgoblins.domain.GameState
import kotlinx.coroutines.delay

@Composable
fun GameScreen() {
    val gameState = remember { GameState() }

    var frameTick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            if (gameState.running) {
                gameState.update(16L)
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
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Not for Goblins", modifier = Modifier.padding(12.dp))
            Text(text = "Score: ${gameState.score}", modifier = Modifier.padding(12.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            GameCanvas(gameState, frameTick)
        }

        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (!gameState.running) {
                Button(onClick = { gameState.restart() }) { Text("Play") }
            } else {
                Button(onClick = { gameState.pause() }) { Text("Pause") }
            }
        }
    }
}

@Composable
fun GameCanvas(gameState: GameState, frameTick: Int) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures { gameState.onTap() }
        }
    ) {
        drawRect(
            color = Color(0xFF7c6b3f),
            topLeft = Offset(0f, size.height - 150f),
            size = Size(size.width, 150f)
        )

        val p = gameState.player
        drawRect(
            color = Color.Magenta,
            topLeft = Offset(p.x, p.y),
            size = Size(p.width, p.height)
        )

        for (obs in gameState.obstacles) {
            drawRect(
                color = Color(0xFF4b8b3b),
                topLeft = Offset(obs.x, obs.y),
                size = Size(obs.width, obs.height)
            )
        }

        if (gameState.gameOver) {
            drawRect(
                color = Color(0x88000000),
                topLeft = Offset(0f, 0f),
                size = size
            )
        }
    }
}
