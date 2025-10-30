package com.melongamesinc.notforgoblins.domain.models.projectile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sqrt

open class Projectile(
    startX: Float,
    startY: Float,
    val targetX: Float,
    val targetY: Float,
    var speed: Float = 400f,
    open val damage: Int = 5
) {
    var x by mutableStateOf(startX)
    var y by mutableStateOf(startY)
    var alive by mutableStateOf(true)

    open fun update(delta: Float) {
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

class SplashProjectile(
    startX: Float,
    startY: Float,
    targetX: Float,
    targetY: Float,
    speed: Float = 300f,
    override val damage: Int = 10,
    val radius: Float = 60f
) : Projectile(startX, startY, targetX, targetY, speed, damage)

class SlowProjectile(
    startX: Float,
    startY: Float,
    targetX: Float,
    targetY: Float,
    speed: Float = 380f,
    override val damage: Int = 3,
    val slowMultiplier: Float = 0.5f,
    val slowDurationMs: Long = 1500L
) : Projectile(startX, startY, targetX, targetY, speed, damage)
