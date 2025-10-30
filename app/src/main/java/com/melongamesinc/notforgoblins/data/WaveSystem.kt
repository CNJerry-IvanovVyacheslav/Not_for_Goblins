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

        val hpMult = 1.12f.pow(wave)
        val spdMult = 1.025f.pow(wave)

        val enemiesPerSpawn = ((spawnMultiplier * 1 + wave * 0.3f)).toInt().coerceAtLeast(1)

        repeat(enemiesPerSpawn) { i ->
            val type = if (wave % 5 == 0 && Random.nextFloat() < 0.4f) EnemyType.TANK
            else chooseEnemyType(wave)

            val baseEnemy: BaseEnemy = when (type) {
                EnemyType.BASIC -> BasicGoblin(state.path, hpMult, spdMult)
                EnemyType.FAST -> FastGoblin(state.path, hpMult, spdMult)
                EnemyType.TANK -> TankGoblin(state.path, hpMult, spdMult)
            }

            val enemy = baseEnemy

            val yOffset = Random.nextFloat() * 100f - 50f
            enemy.x = -Random.nextFloat() * 50f
            enemy.y = state.path.first().second + yOffset

            enemy.spawnDelay = i * 150L
            state.enemies.add(enemy)
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
        spawnMultiplier = 1f * 1.18f.pow(state.waveNumber)
        waveDuration = (6000f + state.waveNumber * 300).coerceAtMost(15000f)
        timer = waveDuration
        nextSpawn = 0f
        timeToNextWave = timer / 1000f
    }
}