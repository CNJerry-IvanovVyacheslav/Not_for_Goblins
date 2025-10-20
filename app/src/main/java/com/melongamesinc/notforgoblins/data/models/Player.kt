package com.melongamesinc.notforgoblins.data.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Player {
    var x by mutableStateOf(200f)
    var y by mutableStateOf(500f)
    val width = 80f
    val height = 120f

    private var vy = 0f
    private val gravity = 0.9f
    private val jumpImpulse = -22f
    private val groundY = 600f

    fun reset() {
        x = 200f
        y = groundY - height
        vy = 0f
    }

    fun jump() {
        if (isOnGround()) vy = jumpImpulse
    }

    fun isOnGround(): Boolean = y >= groundY - height - 0.5f

    fun update(deltaMs: Long) {
        vy += gravity
        y += vy
        if (y > groundY - height) {
            y = groundY - height
            vy = 0f
        }
    }
}
