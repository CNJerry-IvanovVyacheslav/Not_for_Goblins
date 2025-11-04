package com.melongamesinc.notforgoblins.domain.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.pow
import kotlin.math.sqrt

open class BaseEnemy(
    val path: List<Pair<Float, Float>>,
    var speed: Float = 50f,
    var hp: Int = 10,
    open val xpReward: Int = 1
) {
    var x by mutableStateOf(path.first().first)
    var y by mutableStateOf(path.first().second)
    private var pathIndex = 0
    var reachedBase = false

    var spawnDelay: Long = 0
    private var spawnTimer: Long = 0
    private var spawned = false

    private var slowUntilMs: Long = 0
    private var slowMultiplier: Float = 1f
    private var stunUntilMs: Long = 0

    fun applySlow(multiplier: Float, durationMs: Long, nowMs: Long) {
        slowMultiplier = multiplier
        slowUntilMs = nowMs + durationMs
    }

    fun applyStun(durationMs: Long, nowMs: Long) {
        stunUntilMs = nowMs + durationMs
    }

    fun update(delta: Float, nowMs: Long) {
        if (!spawned) {
            spawnTimer += delta.toLong()
            if (spawnTimer < spawnDelay) return
            spawned = true
        }

        if (pathIndex >= path.size - 1) {
            reachedBase = true
            return
        }

        if (nowMs > slowUntilMs) slowMultiplier = 1f

        if (nowMs < stunUntilMs) return

        val (tx, ty) = path[pathIndex + 1]
        val dx = tx - x
        val dy = ty - y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 2f) pathIndex++
        else {
            val step = (speed * slowMultiplier * delta / 1000f)
            x += (dx / dist) * step
            y += (dy / dist) * step
        }
    }

    fun nextTarget(): Pair<Float, Float> {
        return path.getOrElse(pathIndex + 1) { path.last() }
    }

    open fun hitBy(projectile: Projectile): Boolean {
        val d = sqrt((x - projectile.x).pow(2) + (y - projectile.y).pow(2))
        return d < 16f
    }
}

class BasicGoblin(path: List<Pair<Float, Float>>, hpMult: Float, spdMult: Float) :
    BaseEnemy(path, speed = 60f * spdMult, hp = (10 * hpMult).toInt(), xpReward = 1)

class FastGoblin(path: List<Pair<Float, Float>>, hpMult: Float, spdMult: Float) :
    BaseEnemy(path, speed = 90f * spdMult, hp = (6 * hpMult).toInt(), xpReward = 2)

class TankGoblin(path: List<Pair<Float, Float>>, hpMult: Float, spdMult: Float) :
    BaseEnemy(path, speed = 35f * spdMult, hp = (30 * hpMult).toInt(), xpReward = 5)