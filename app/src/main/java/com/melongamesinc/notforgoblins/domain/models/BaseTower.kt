package com.melongamesinc.notforgoblins.domain.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.pow
import kotlin.math.sqrt

abstract class BaseTower(
    var x: Float,
    var y: Float,
    var range: Float = 200f,
    var damage: Int = 5,
    var fireRate: Float = 1.0f
) {
    protected var cooldown by mutableStateOf(0f)
    var level by mutableStateOf(1)

    open fun updateCooldown(delta: Float) {
        if (cooldown > 0f) cooldown -= delta / 1000f
    }

    abstract fun update(
        delta: Float,
        enemies: List<BaseEnemy>,
        projectiles: MutableList<Projectile>
    )

    fun distTo(e: BaseEnemy): Float {
        val dx = e.x - x
        val dy = e.y - y
        return sqrt(dx * dx + dy * dy)
    }

    fun upgradeCost(playerLevel: Int = 1, baseCost: Int = 20): Int {
        val towerMultiplier = 2.0.pow((level - 1).toDouble())
        val playerMultiplier = 1 + playerLevel * 0.05
        return (baseCost * towerMultiplier * playerMultiplier).toInt()
    }

    open fun upgrade() {
        level++

        damage += (3 * 1.05.pow(level - 1)).toInt()
        range += 20f * 1.02.pow(level - 1).toFloat()
        fireRate *= 1.05f
    }

    fun isClicked(px: Float, py: Float, size: Float = 50f): Boolean {
        val half = size / 2
        return px in (x - half)..(x + half) && py in (y - half)..(y + half)
    }
}

class BasicBallista(x: Float, y: Float) :
    BaseTower(x, y, range = 200f, damage = 6, fireRate = 1.2f) {
    override fun update(
        delta: Float,
        enemies: List<BaseEnemy>,
        projectiles: MutableList<Projectile>
    ) {
        updateCooldown(delta)
        if (cooldown > 0f) return
        val target = enemies.minByOrNull { distTo(it) }
        if (target != null && distTo(target) <= range) {
            projectiles.add(Projectile(x, y, target.x, target.y, speed = 500f, damage = damage))
            cooldown = 1f / fireRate
        }
    }
}

class SplashTower(x: Float, y: Float) :
    BaseTower(x, y, range = 260f, damage = 10, fireRate = 0.3f) {
    override fun update(
        delta: Float,
        enemies: List<BaseEnemy>,
        projectiles: MutableList<Projectile>
    ) {
        updateCooldown(delta)
        if (cooldown > 0f) return
        val target = enemies.minByOrNull { distTo(it) }
        if (target != null && distTo(target) <= range) {
            projectiles.add(
                SplashProjectile(
                    x,
                    y,
                    target.x,
                    target.y,
                    speed = 320f,
                    damage = damage,
                    radius = 50f
                )
            )
            cooldown = 1f / fireRate
        }
    }

    override fun upgrade() {
        super.upgrade()
        damage += 4
    }
}

class SniperTower(x: Float, y: Float) :
    BaseTower(x, y, range = 600f, damage = 25, fireRate = 0.25f) {
    override fun update(
        delta: Float,
        enemies: List<BaseEnemy>,
        projectiles: MutableList<Projectile>
    ) {
        updateCooldown(delta)
        if (cooldown > 0f) return
        val target = enemies.filter { distTo(it) <= range }.maxByOrNull { it.hp }
        if (target != null) {
            projectiles.add(Projectile(x, y, target.x, target.y, speed = 900f, damage = damage))
            cooldown = 1f / fireRate
        }
    }
}

class SlowTower(x: Float, y: Float) :
    BaseTower(x, y, range = 180f, damage = 2, fireRate = 1.0f) {
    override fun update(
        delta: Float,
        enemies: List<BaseEnemy>,
        projectiles: MutableList<Projectile>
    ) {
        updateCooldown(delta)
        if (cooldown > 0f) return
        val target = enemies.minByOrNull { distTo(it) }
        if (target != null && distTo(target) <= range) {
            projectiles.add(
                SlowProjectile(
                    x,
                    y,
                    target.x,
                    target.y,
                    speed = 360f,
                    damage = damage,
                    slowMultiplier = 0.5f,
                    slowDurationMs = 1500L
                )
            )
            cooldown = 1f / fireRate
        }
    }
}
