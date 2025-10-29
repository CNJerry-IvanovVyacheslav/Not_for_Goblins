package com.melongamesinc.notforgoblins.data

import android.util.Log
import androidx.compose.runtime.*
import com.melongamesinc.notforgoblins.domain.models.*
import com.melongamesinc.notforgoblins.utils.PathTemplates
import kotlin.math.pow

class GameState {
    var running by mutableStateOf(false)
    var awaitingTowerPlacement by mutableStateOf(false)
    var placingStartingTower by mutableStateOf(false)
    var waveNumber by mutableStateOf(0)
    var gold by mutableStateOf(100)
    var baseHealth by mutableStateOf(20)
    var showCardChoice by mutableStateOf(false)
    var score by mutableStateOf(0)

    val enemies = mutableStateListOf<Enemy>()
    val towers = mutableStateListOf<Tower>()
    val projectiles = mutableStateListOf<Projectile>()
    val path = PathTemplates.basicPath()

    val towerSlots = listOf(
        100f to 400f,
        300f to 420f,
        600f to 300f,
        750f to 300f,
        400f to 250f
    )

    var availableTowers by mutableStateOf(1)
    var cardChoices by mutableStateOf<List<Card>>(emptyList())
        private set

    private var waveManager = WaveManager(this)
    private var cardManager = CardManager(this)

    fun startGame() {
        if (baseHealth <= 0) reset()
        if (availableTowers > 0) {
            placingStartingTower = true
            showCardChoice = false
            running = false
        } else {
            running = true
            placingStartingTower = false
        }
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
        availableTowers = 1
        placingStartingTower = false
        awaitingTowerPlacement = false
    }

    fun placeTower(slotIndex: Int) {
        if (slotIndex !in towerSlots.indices) return
        if (!isSlotFree(slotIndex)) return

        val (x, y) = towerSlots[slotIndex]
        towers.add(Tower(x, y))
        availableTowers--
        placingStartingTower = false

        if (awaitingTowerPlacement) {
            awaitingTowerPlacement = false
            nextWave()
        } else {
            running = true
            nextWave()
        }
    }

    fun nextWave() {
        waveNumber++
        waveManager.spawnWave(waveNumber)
        running = true
        showCardChoice = false
        cardChoices = emptyList()
    }

    fun isSlotFree(index: Int) = towerSlots[index].let { (sx, sy) ->
        towers.none { t -> (t.x - sx).pow(2) + (t.y - sy).pow(2) < 1f }
    }

    fun addTowerAtSlot(index: Int) {
        if (index in towerSlots.indices && isSlotFree(index)) {
            val (x, y) = towerSlots[index]
            towers.add(Tower(x, y))
            running = true
        }
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
            } else if (!p.alive) deadProjectiles.add(p)
        }
        projectiles.removeAll(deadProjectiles)

        if (enemies.isEmpty() && waveManager.finishedSpawning) {
            if (!awaitingTowerPlacement) {
                cardChoices = cardManager.generateThree()
                showCardChoice = true
                running = false
            }
        }

        if (baseHealth <= 0) running = false
    }

    fun applyCard(card: Card) {
        cardManager.applyCard(card)
        showCardChoice = false
        cardChoices = emptyList()
        if (card.effectType == CardEffectType.ADD_TOWER) {
            awaitingTowerPlacement = true
        } else {
            nextWave()
        }
    }
}