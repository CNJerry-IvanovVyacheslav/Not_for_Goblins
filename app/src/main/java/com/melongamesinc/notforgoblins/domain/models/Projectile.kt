package com.melongamesinc.notforgoblins.domain.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sqrt

class Projectile(
    startX: Float,
    startY: Float,
    val targetX: Float,
    val targetY: Float,
    var speed: Float = 400f,
    val damage: Int = 5
) {
    var x by mutableStateOf(startX)
    var y by mutableStateOf(startY)
    var alive by mutableStateOf(true)

    fun update(delta: Float) {
        if (!alive) return
        val dx = targetX - x
        val dy = targetY - y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 6f) {
            alive = false
            return
        }
        val step = speed * delta / 1000f
        x += (dx / dist) * step
        y += (dy / dist) * step
    }
}
