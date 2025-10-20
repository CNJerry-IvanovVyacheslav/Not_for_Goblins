package com.melongamesinc.notforgoblins.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable


private val DarkColorPalette = darkColors(
    primary = androidx.compose.ui.graphics.Color(0xFF2E7D32)
)


@Composable
fun NotForGoblinsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DarkColorPalette,
        content = content
    )
}