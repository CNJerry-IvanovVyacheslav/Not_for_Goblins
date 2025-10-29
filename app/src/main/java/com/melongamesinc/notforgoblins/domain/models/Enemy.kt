package com.melongamesinc.notforgoblins.domain.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.pow
import kotlin.math.sqrt

class Enemy(
    val path: List<Pair<Float, Float>>,
    var speed: Float = 50f,
    var hp: Int = 10
) {
    var x by mutableStateOf(path.first().first)
    var y by mutableStateOf(path.first().second)
    private var pathIndex = 0
    var reachedBase = false

    fun update(delta: Float) {
        if (pathIndex >= path.size - 1) {
            reachedBase = true
            return
        }

        val (tx, ty) = path[pathIndex + 1]
        val dx = tx - x
        val dy = ty - y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 2f) {
            pathIndex++
        } else {
            val step = (speed * delta / 1000f)
            x += (dx / dist) * step
            y += (dy / dist) * step
        }
    }

    fun hitBy(projectile: Projectile): Boolean {
        val d = sqrt((x - projectile.x).pow(2) + (y - projectile.y).pow(2))
        return d < 16f
    }
}
