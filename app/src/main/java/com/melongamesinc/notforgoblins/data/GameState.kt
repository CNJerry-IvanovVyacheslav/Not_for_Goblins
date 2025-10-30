package com.melongamesinc.notforgoblins.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melongamesinc.notforgoblins.domain.models.Card
import com.melongamesinc.notforgoblins.domain.models.CardEffectType
import com.melongamesinc.notforgoblins.domain.models.BaseEnemy
import com.melongamesinc.notforgoblins.domain.models.BaseTower
import com.melongamesinc.notforgoblins.domain.models.BasicBallista
import com.melongamesinc.notforgoblins.domain.models.BasicGoblin
import com.melongamesinc.notforgoblins.domain.models.FastGoblin
import com.melongamesinc.notforgoblins.domain.models.SlowTower
import com.melongamesinc.notforgoblins.domain.models.SniperTower
import com.melongamesinc.notforgoblins.domain.models.SplashTower
import com.melongamesinc.notforgoblins.domain.models.TankGoblin
import com.melongamesinc.notforgoblins.domain.models.projectile.Projectile
import com.melongamesinc.notforgoblins.utils.PathTemplates
import kotlin.math.pow

class GameState {

    var mapName by mutableStateOf("Goblin Swamps")

    var running by mutableStateOf(false)
    var awaitingTowerPlacement by mutableStateOf(false)
    var placingStartingTower by mutableStateOf(false)
    var waveNumber by mutableStateOf(0)
    var gold by mutableStateOf(100)
    var baseHealth by mutableStateOf(20)
    var showCardChoice by mutableStateOf(false)
    var score by mutableStateOf(0)

    // 🆕 Прогресс игрока
    var experience by mutableStateOf(0)
    var level by mutableStateOf(1)
    var nextLevelXp by mutableStateOf(10)

    val enemies = mutableStateListOf<BaseEnemy>()
    val towers = mutableStateListOf<BaseTower>()
    val projectiles = mutableStateListOf<Projectile>()
    val path = PathTemplates.basicPath()
    val timeToNextWave get() = waveManager.timeToNextWave

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

    val unlockedTowerTypes = mutableStateListOf<String>("BASIC")

    private var waveManager = WaveSystem(this)
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
        experience = 0
        level = 1
        nextLevelXp = 10
        cardChoices = emptyList()
        showCardChoice = false
        availableTowers = 1
        placingStartingTower = false
        awaitingTowerPlacement = false
        unlockedTowerTypes.clear()
        unlockedTowerTypes.add("BASIC")
        waveManager = WaveSystem(this)
        cardManager = CardManager(this)
    }

    fun placeTower(slotIndex: Int, towerType: String = "BASIC") {
        if (slotIndex !in towerSlots.indices) return
        if (!isSlotFree(slotIndex)) return

        val (x, y) = towerSlots[slotIndex]
        val tower = when (towerType) {
            "SPLASH" -> SplashTower(x, y)
            "SNIPER" -> SniperTower(x, y)
            "SLOW" -> SlowTower(x, y)
            else -> BasicBallista(x, y)
        }
        towers.add(tower)
        availableTowers--
        placingStartingTower = false
        awaitingTowerPlacement = false

        running = true
        waveManager.start()
    }

    fun isSlotFree(index: Int) = towerSlots[index].let { (sx, sy) ->
        towers.none { t -> (t.x - sx).pow(2) + (t.y - sy).pow(2) < 1f }
    }

    fun addTowerAtSlot(index: Int) {
        if (index in towerSlots.indices && isSlotFree(index)) {
            val (x, y) = towerSlots[index]
            towers.add(BasicBallista(x, y))
            running = true
        }
    }

    fun update(delta: Float) {
        waveManager.update(delta)
        if (!running) return

        val deadEnemies = mutableListOf<BaseEnemy>()
        for (e in enemies) {
            e.update(delta)
            if (e.reachedBase) {
                baseHealth--
                deadEnemies.add(e)
            } else if (e.hp <= 0) {
                deadEnemies.add(e)
                score += 10
                gold += 5
                addExperienceFor(e)
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
                when (p) {
                    is com.melongamesinc.notforgoblins.domain.models.projectile.SplashProjectile -> {
                        for (e2 in enemies) {
                            val dx = e2.x - p.x
                            val dy = e2.y - p.y
                            if (dx * dx + dy * dy <= p.radius * p.radius) {
                                e2.hp -= p.damage
                            }
                        }
                    }

                    is com.melongamesinc.notforgoblins.domain.models.projectile.SlowProjectile -> {
                        val now = System.currentTimeMillis()
                        hit.applySlow(p.slowMultiplier, p.slowDurationMs, now)
                    }
                }
                deadProjectiles.add(p)
            } else if (!p.alive) deadProjectiles.add(p)
        }
        projectiles.removeAll(deadProjectiles)

        if (baseHealth <= 0) running = false
    }

    private fun addExperienceFor(enemy: BaseEnemy) {
        val gained = when (enemy) {
            is BasicGoblin -> 2
            is FastGoblin -> 3
            is TankGoblin -> 5
            else -> 1
        }
        experience += gained
        Log.d("XP", "Got $gained XP (total $experience/$nextLevelXp)")
        checkLevelUp()
    }

    private fun checkLevelUp() {
        while (experience >= nextLevelXp) {
            experience -= nextLevelXp
            level++
            nextLevelXp = (nextLevelXp * 1.5f).toInt().coerceAtLeast(10)
            showLevelUpCards()
        }
    }

    private fun showLevelUpCards() {
        cardChoices = cardManager.generateThree()
        showCardChoice = true
        running = false
    }

    fun applyCard(card: Card) {
        cardManager.applyCard(card)
        showCardChoice = false
        cardChoices = emptyList()
        if (card.effectType == CardEffectType.ADD_TOWER) {
            awaitingTowerPlacement = true
        } else {
            running = true
        }
    }

    fun upgradeTowerAt(x: Float, y: Float): Boolean {
        val tower = towers.minByOrNull { t -> (t.x - x) * (t.x - x) + (t.y - y) * (t.y - y) }
        if (tower != null) {
            val cost = 100 * tower.level
            if (gold >= cost) {
                gold -= cost
                tower.upgrade()
                return true
            }
        }
        return false
    }
}
