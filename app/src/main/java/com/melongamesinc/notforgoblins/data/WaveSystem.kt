package com.melongamesinc.notforgoblins.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melongamesinc.notforgoblins.domain.models.BaseEnemy
import com.melongamesinc.notforgoblins.domain.models.BasicGoblin
import com.melongamesinc.notforgoblins.domain.models.FastGoblin
import com.melongamesinc.notforgoblins.domain.models.TankGoblin
import kotlin.math.pow
import kotlin.random.Random

class WaveSystem(private val state: GameState) {

    var timeToNextWave by mutableStateOf(0f)
        private set

    private var waveDuration = 10000f
    private var spawnMultiplier = 1f
    private var restTime = 3000f
    private var spawnInterval = 500f
    private var timer = 0f
    private var nextSpawn = 0f

    fun start() {
        timer = waveDuration
        nextSpawn = 0f
        timeToNextWave = timer / 1000f
    }

    fun update(delta: Float) {
        if (!state.running) return

        timer -= delta
        nextSpawn -= delta
        timeToNextWave = (timer / 1000f).coerceAtLeast(0f)

        if (nextSpawn <= 0f) {
            spawnEnemy()
            nextSpawn = spawnInterval
        }

        if (timer <= 0f) startNextWave()

        if (state.enemies.isEmpty() && timer > restTime) {
            timer = restTime
        }
    }

    private fun spawnEnemy() {
        val wave = state.waveNumber

        val hpMult = 1.08f.pow(wave)
        val spdMult = 1.015f.pow(wave)

        val maxEnemiesPerSpawn = when {
            wave < 5 -> 2
            wave < 10 -> 3
            else -> 4
        }
        val enemiesPerSpawn = ((1 + wave * 0.1f)).toInt().coerceAtMost(maxEnemiesPerSpawn)

        repeat(enemiesPerSpawn) { i ->
            val type = if (wave % 5 == 0 && Random.nextFloat() < 0.2f) EnemyType.TANK
            else chooseEnemyType(wave)

            val baseEnemy: BaseEnemy = when (type) {
                EnemyType.BASIC -> BasicGoblin(state.path, hpMult, spdMult)
                EnemyType.FAST -> FastGoblin(state.path, hpMult, spdMult)
                EnemyType.TANK -> TankGoblin(state.path, hpMult, spdMult)
            }

            val yOffset = Random.nextFloat() * 80f - 40f
            baseEnemy.x = -Random.nextFloat() * 50f
            baseEnemy.y = state.path.first().second + yOffset
            baseEnemy.spawnDelay = i * 250L

            state.addEnemy(baseEnemy)
        }
    }


    private fun chooseEnemyType(wave: Int): EnemyType {
        val r = Random.nextFloat()
        return when {
            wave < 3 -> EnemyType.BASIC
            wave < 6 -> if (r < 0.15f) EnemyType.FAST else EnemyType.BASIC
            else -> when {
                r < 0.1f -> EnemyType.TANK
                r < 0.3f -> EnemyType.FAST
                else -> EnemyType.BASIC
            }
        }
    }

    private fun startNextWave() {
        state.waveNumber++

        spawnMultiplier = 1f + state.waveNumber * 0.05f
        waveDuration = (7000f + state.waveNumber * 200).coerceAtMost(12000f)
        spawnInterval =
            (500f + state.waveNumber * 50f).coerceAtMost(1200f)

        timer = waveDuration
        nextSpawn = 0f
        timeToNextWave = timer / 1000f
    }
}