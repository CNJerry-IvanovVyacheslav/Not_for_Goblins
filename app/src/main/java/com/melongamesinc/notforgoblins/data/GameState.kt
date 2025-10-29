package com.melongamesinc.notforgoblins.data

import android.util.Log
import androidx.compose.runtime.*
import com.melongamesinc.notforgoblins.domain.models.*
import com.melongamesinc.notforgoblins.utils.PathTemplates

class GameState {

    val maxTowerSlots = 5
    val towerSlots = mutableListOf<Pair<Float, Float>>()

    init {
        towerSlots.add(400f to 400f)
        towerSlots.add(600f to 300f)
        towerSlots.add(700f to 500f)
        towerSlots.add(900f to 250f)
        towerSlots.add(1000f to 350f)
    }

    var running by mutableStateOf(false)
    var waveNumber by mutableStateOf(0)
    var gold by mutableStateOf(100)
    var baseHealth by mutableStateOf(20)
    var showCardChoice by mutableStateOf(false)
    var score by mutableStateOf(0)

    val enemies = mutableStateListOf<Enemy>()
    val towers = mutableStateListOf<Tower>()
    val projectiles = mutableStateListOf<Projectile>()
    val path = PathTemplates.basicPath()

    private var waveManager = WaveManager(this)
    private var cardManager = CardManager(this)

    var cardChoices by mutableStateOf<List<Card>>(emptyList())
        private set

    fun startGame() {
        reset()
        towers.add(Tower(towerSlots[0].first, towerSlots[0].second))
        running = true
        nextWave()
    }

    fun reset() {
        enemies.clear()
        towers.clear()
        projectiles.clear()
        waveNumber = 0
        baseHealth = 20
        gold = 100
        score = 0
        cardChoices = emptyList()
        showCardChoice = false
    }

    fun nextWave() {
        waveNumber++
        Log.d("Wave", "Starting wave $waveNumber")
        waveManager.spawnWave(waveNumber)
        running = true
        showCardChoice = false
        cardChoices = emptyList()
    }

    fun update(delta: Float) {
        waveManager.update(delta)

        if (!running) return

        val deadEnemies = mutableListOf<Enemy>()
        for (e in enemies) {
            e.update(delta)
            if (e.reachedBase) {
                baseHealth--
                deadEnemies.add(e)
                Log.d("Enemy", "Enemy reached base! HP=$baseHealth")
            } else if (e.hp <= 0) {
                deadEnemies.add(e)
                score += 10
                gold += 5
            }
        }
        enemies.removeAll(deadEnemies)

        for (t in towers) t.update(delta, enemies, projectiles)

        val deadProjectiles = mutableListOf<Projectile>()
        for (p in projectiles) {
            p.update(delta)
            val hit = enemies.firstOrNull { it.hitBy(p) }
            if (hit != null) {
                hit.hp -= p.damage
                deadProjectiles.add(p)
            } else if (!p.alive) {
                deadProjectiles.add(p)
            }
        }
        projectiles.removeAll(deadProjectiles)

        if (enemies.isEmpty() && waveManager.finishedSpawning) {
            val cards = cardManager.generateThree()
            cardChoices = cards
            showCardChoice = true
            running = false
            Log.d("GameState", "Wave $waveNumber finished — show cards")
        }

        if (baseHealth <= 0) {
            running = false
            Log.d("Game", "Game Over!")
        }
    }

    fun applyCard(card: Card) {
        cardManager.applyCard(card)
        showCardChoice = false
        cardChoices = emptyList()
        nextWave()
    }
}
