package com.melongamesinc.notforgoblins.domain.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sqrt

class Tower(
    var x: Float,
    var y: Float,
    var range: Float = 200f,
    var damage: Int = 5,
    var fireRate: Float = 1.0f
) {
    private var cooldown by mutableStateOf(0f)

    fun update(delta: Float, enemies: List<Enemy>, projectiles: MutableList<Projectile>) {
        if (cooldown > 0f) {
            cooldown -= delta / 1000f
        }
        if (cooldown <= 0f) {
            val target = enemies.minByOrNull { distTo(it) }
            if (target != null && distTo(target) <= range) {
                // fire
                val p = Projectile(x, y, target.x, target.y, speed = 500f, damage = damage)
                projectiles.add(p)
                cooldown = 1f / fireRate
            }
        }
    }

    private fun distTo(e: Enemy): Float {
        val dx = e.x - x
        val dy = e.y - y
        return sqrt(dx * dx + dy * dy)
    }
}
