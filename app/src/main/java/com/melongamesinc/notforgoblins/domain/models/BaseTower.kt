package com.melongamesinc.notforgoblins.domain.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melongamesinc.notforgoblins.domain.models.projectile.Projectile
import com.melongamesinc.notforgoblins.domain.models.projectile.SplashProjectile
import com.melongamesinc.notforgoblins.domain.models.projectile.SlowProjectile
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

    open fun upgrade() {
        level++
        damage += 3
        range += 20f
        fireRate *= 1.1f
    }
}

// Basic single-target
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

// Splash / mortar
class SplashTower(x: Float, y: Float) :
    BaseTower(x, y, range = 260f, damage = 12, fireRate = 0.45f) {
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
                    radius = 70f
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

class SlowTower(x: Float, y: Float) : BaseTower(x, y, range = 180f, damage = 2, fireRate = 1.0f) {
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

    override fun upgrade() {
        super.upgrade()
    }
}
