package com.melongamesinc.notforgoblins.domain

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.melongamesinc.notforgoblins.data.models.Obstacle
import com.melongamesinc.notforgoblins.data.models.Player
import kotlin.random.Random

class GameState {
    var running by mutableStateOf(false)
    var gameOver by mutableStateOf(false)
    var score by mutableStateOf(0)

    val player = Player()
    val obstacles: SnapshotStateList<Obstacle> = SnapshotStateList()

    private var spawnTimer = 0L

    fun restart() {
        Log.d("GameState", "Restarting game...")
        player.reset()
        obstacles.clear()
        score = 0
        running = true
        gameOver = false
        spawnTimer = 0
        spawnInitialObstacles()
    }

    fun pause() {
        running = false
        Log.d("GameState", "Paused")
    }

    fun onTap() {
        if (!running) return
        Log.d("Input", "Player jump triggered")
        player.jump()
    }

    fun update(deltaMs: Long) {
        if (!running) return

        player.update(deltaMs)

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val o = iterator.next()
            o.x -= 6f + score * 0.01f
            if (o.x + o.width < 0f) {
                iterator.remove()
                score += 10
                Log.d("Score", "Obstacle passed! score=$score")
            }

            if (checkCollision(player, o)) {
                running = false
                gameOver = true
                Log.d("Collision", "Player collided with obstacle at x=${o.x}")
            }
        }

        spawnTimer += deltaMs
        if (spawnTimer > 2000) {
            spawnObstacle()
            spawnTimer = 0
            Log.d("Spawner", "Spawned obstacle. total=${obstacles.size}")
        }
    }

    private fun spawnInitialObstacles() {
        var x = 800f
        for (i in 0 until 3) {
            obstacles.add(Obstacle(x, 600f))
            x += 600f
        }
        Log.d("Spawner", "Initial obstacles spawned: ${obstacles.size}")
    }

    private fun spawnObstacle() {
        val startX = 1200f + Random.Default.nextInt(0, 400)
        obstacles.add(Obstacle(startX.toFloat(), 600f))
    }

    private fun checkCollision(p: Player, o: Obstacle): Boolean {
        val px1 = p.x
        val py1 = p.y
        val px2 = p.x + p.width
        val py2 = p.y + p.height

        val ox1 = o.x
        val oy1 = o.y
        val ox2 = o.x + o.width
        val oy2 = o.y + o.height

        return !(px2 < ox1 || px1 > ox2 || py2 < oy1 || py1 > oy2)
    }
}