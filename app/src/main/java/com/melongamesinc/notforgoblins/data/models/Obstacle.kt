package com.melongamesinc.notforgoblins.data.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Obstacle(xStart: Float = 0f, yStart: Float = 0f) {
    var x by mutableStateOf(xStart)
    var y by mutableStateOf(yStart)
    val width = 100f
    val height = 100f
}