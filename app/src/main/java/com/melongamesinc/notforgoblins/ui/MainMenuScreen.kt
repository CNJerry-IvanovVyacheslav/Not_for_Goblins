package com.melongamesinc.notforgoblins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onOpenShop: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF494949)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Not For Goblins", fontSize = 32.sp, color = Color.White)
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onStartGame, modifier = Modifier.width(200.dp)) {
                Text("▶ Start Game")
            }

            Button(onClick = onOpenShop, modifier = Modifier.width(200.dp)) {
                Text("🏪 Shop / Upgrades")
            }

            Button(onClick = onExit, modifier = Modifier.width(200.dp)) {
                Text("❌ Exit")
            }
        }
    }
}
