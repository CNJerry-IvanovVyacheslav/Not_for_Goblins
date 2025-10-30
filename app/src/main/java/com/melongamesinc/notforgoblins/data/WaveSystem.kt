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
    private var restTime = 3000f
    private var spawnInterval = 2000f
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

        if (timer <= 0f) {
            startNextWave()
        }

        if (state.enemies.isEmpty() && timer > restTime) {
            timer = restTime
        }
    }

    private fun spawnEnemy() {
        val wave = state.waveNumber
        val type = chooseEnemyType(wave)
        val hpMult = 1.05f.pow(wave)
        val spdMult = 1.01f.pow(wave)
        val enemy: BaseEnemy = when (type) {
            EnemyType.BASIC -> BasicGoblin(state.path, hpMult, spdMult)
            EnemyType.FAST -> FastGoblin(state.path, hpMult, spdMult)
            EnemyType.TANK -> TankGoblin(state.path, hpMult, spdMult)
        }
        state.enemies.add(enemy)
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
        waveDuration = (10000f + state.waveNumber * 500).coerceAtMost(20000f)
        timer = waveDuration
        nextSpawn = 0f
        timeToNextWave = timer / 1000f
    }

    private enum class EnemyType { BASIC, FAST, TANK }
}
