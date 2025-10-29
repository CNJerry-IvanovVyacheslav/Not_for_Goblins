package com.melongamesinc.notforgoblins.data

import android.util.Log
import com.melongamesinc.notforgoblins.domain.models.Enemy
import kotlin.math.pow
import kotlin.random.Random

class WaveManager(private val state: GameState) {
    private var spawnQueue = mutableListOf<Pair<Float, Int>>()
    var finishedSpawning = false
        private set

    private var localTimer = 0f

    fun spawnWave(waveNumber: Int) {
        spawnQueue.clear()
        finishedSpawning = false

        val baseCount = if (waveNumber <= 30) {
            waveNumber + (0..2).random()
        } else {
            (30 * 1.07.pow((waveNumber - 30).toDouble())).toInt()
        }

        var timeAcc = 0f
        for (i in 0 until baseCount) {
            val hp = (5 + waveNumber * 1.5).toInt()
            val delay = 500f + Random.nextInt(0, 700)
            timeAcc += delay
            spawnQueue.add(timeAcc to hp)
        }
        localTimer = 0f
        Log.d("WaveManager", "Prepared wave $waveNumber with ${spawnQueue.size} enemies")
    }

    fun update(delta: Float) {
        if (spawnQueue.isEmpty()) {
            finishedSpawning = true
            return
        }
        localTimer += delta
        val toSpawn = mutableListOf<Pair<Float, Int>>()
        val remaining = mutableListOf<Pair<Float, Int>>()
        for (pair in spawnQueue) {
            if (pair.first <= localTimer) toSpawn.add(pair) else remaining.add(pair)
        }
        spawnQueue = remaining
        for ((_, hp) in toSpawn) {
            spawnEnemy(hp)
        }
        finishedSpawning = spawnQueue.isEmpty()
    }

    private fun spawnEnemy(hp: Int) {
        val enemy = Enemy(state.path, speed = 60f + Random.nextInt(-10, 20), hp = hp)
        state.enemies.add(enemy)
        Log.d("WaveManager", "Spawned enemy (hp=$hp). total=${state.enemies.size}")
    }
}
