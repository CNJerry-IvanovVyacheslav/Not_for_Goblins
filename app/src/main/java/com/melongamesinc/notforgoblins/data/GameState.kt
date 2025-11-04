package com.melongamesinc.notforgoblins.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melongamesinc.notforgoblins.domain.models.BaseEnemy
import com.melongamesinc.notforgoblins.domain.models.BaseTower
import com.melongamesinc.notforgoblins.domain.models.BasicBallista
import com.melongamesinc.notforgoblins.domain.models.BasicGoblin
import com.melongamesinc.notforgoblins.domain.models.Card
import com.melongamesinc.notforgoblins.domain.models.CardEffectType
import com.melongamesinc.notforgoblins.domain.models.FastGoblin
import com.melongamesinc.notforgoblins.domain.models.Projectile
import com.melongamesinc.notforgoblins.domain.models.SlowProjectile
import com.melongamesinc.notforgoblins.domain.models.SlowTower
import com.melongamesinc.notforgoblins.domain.models.SniperTower
import com.melongamesinc.notforgoblins.domain.models.SplashProjectile
import com.melongamesinc.notforgoblins.domain.models.SplashTower
import com.melongamesinc.notforgoblins.domain.models.TankGoblin
import com.melongamesinc.notforgoblins.utils.PathTemplates
import kotlin.math.pow

class GameState {

    var mapName by mutableStateOf("Goblin Swamps")
    var running by mutableStateOf(false)
    var awaitingTowerPlacement by mutableStateOf(false)
    var placingStartingTower by mutableStateOf(false)
    var waveNumber by mutableStateOf(0)
    var gold by mutableStateOf(100)
    var towerToPlaceOptions by mutableStateOf<List<String>>(emptyList())
    var towerToPlace by mutableStateOf<String?>(null)
    var globalCritChance: Float? by mutableStateOf(0f)
    var goldMultiplier: Float = 1f
    var xpMultiplier: Float = 1f
    var baseHealth by mutableStateOf(20)
    var globalSlowMultiplier: Float? = null
    var globalSlowUntilMs: Long? = null
    var showCardChoice by mutableStateOf(false)
    var score by mutableStateOf(0)

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

    val unlockedTowerTypes = mutableStateListOf("BASIC")

    var globalFireRateMultiplier = 1.0f
    var globalDamageBonus = 0
    var globalRangeBonus = 0f

    var towerToUpgrade: BaseTower? by mutableStateOf(null)
    var showUpgradeModal by mutableStateOf(false)

    private var waveManager = WaveSystem(this)
    private var cardManager = CardManager(this)

    fun startGame() {
        if (baseHealth <= 0) reset()
        if (availableTowers > 0) {
            placingStartingTower = true
            showCardChoice = false
            running = false
        } else {
            if (!showUpgradeModal && !showCardChoice) running = true
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
        towerToPlace = null
        unlockedTowerTypes.clear()
        unlockedTowerTypes.add("BASIC")

        globalFireRateMultiplier = 1.0f
        globalDamageBonus = 0
        globalRangeBonus = 0f
        goldMultiplier = 1f
        xpMultiplier = 1f

        towerToUpgrade = null
        showUpgradeModal = false

        waveManager = WaveSystem(this)
        cardManager = CardManager(this)

        cardManager.reset()
    }


    fun placeTower(slotIndex: Int, towerType: String = "BASIC") {
        if (slotIndex !in towerSlots.indices || !isSlotFree(slotIndex)) return

        val (x, y) = towerSlots[slotIndex]
        val tower = when (towerType) {
            "SPLASH" -> SplashTower(x, y)
            "SNIPER" -> SniperTower(x, y)
            "SLOW" -> SlowTower(x, y)
            else -> BasicBallista(x, y)
        }.apply {
            fireRate *= globalFireRateMultiplier
            damage += globalDamageBonus
            range += globalRangeBonus
        }

        towers.add(tower)
        availableTowers--
        placingStartingTower = false
        awaitingTowerPlacement = false
        towerToPlace = null

        if (pendingLevelUps.isNotEmpty()) {
            processNextLevelUp()
        } else if (!showUpgradeModal) {
            running = true
        }

        waveManager.start()
    }


    fun isSlotFree(index: Int) = towerSlots[index].let { (sx, sy) ->
        towers.none { t -> (t.x - sx).pow(2) + (t.y - sy).pow(2) < 1f }
    }

    fun update(delta: Float, nowMs: Long = System.currentTimeMillis()) {
        waveManager.update(delta)
        if (!running) return

        val deadEnemies = mutableListOf<BaseEnemy>()
        for (e in enemies) {
            e.update(delta, nowMs)
            if (e.reachedBase) {
                baseHealth--
                deadEnemies.add(e)
            } else if (e.hp <= 0) {
                deadEnemies.add(e)
                score += 10
                gold += (5 * goldMultiplier).toInt()
                addExperienceFor(e)
            }
        }
        enemies.removeAll(deadEnemies)

        for (t in towers) t.update(delta, enemies, projectiles, globalCrit = globalCritChance ?: 0f)

        val deadProjectiles = mutableListOf<Projectile>()
        for (p in projectiles) {
            p.update(delta)

            val dx = p.x - p.targetX
            val dy = p.y - p.targetY
            val reachedTarget = dx * dx + dy * dy < 9f

            when (p) {
                is SplashProjectile -> {
                    val hit = enemies.firstOrNull { it.hitBy(p) }
                    if (hit != null || reachedTarget) {
                        val enemiesHit = enemies.filter {
                            val ex = it.x - p.x
                            val ey = it.y - p.y
                            ex * ex + ey * ey <= p.radius * p.radius
                        }
                        enemiesHit.forEach { it.hp -= p.damage }
                        p.alive = false
                    }
                }

                is SlowProjectile -> {
                    val hit = enemies.firstOrNull { it.hitBy(p) }
                    if (hit != null) {
                        hit.hp -= p.damage
                        hit.applySlow(p.slowMultiplier, p.slowDurationMs, nowMs)
                        p.alive = false
                    } else if (reachedTarget) {
                        p.alive = false
                    }
                }

                else -> {
                    val hit = enemies.firstOrNull { it.hitBy(p) }
                    if (hit != null) {
                        hit.hp -= p.damage
                        p.alive = false
                    } else if (reachedTarget) {
                        p.alive = false
                    }
                }
            }

            if (!p.alive) deadProjectiles.add(p)
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
        experience += (gained * xpMultiplier).toInt()
        checkLevelUp()
    }

    private val pendingLevelUps = mutableListOf<Unit>()
    fun checkLevelUp() {
        while (experience >= nextLevelXp) {
            experience -= nextLevelXp
            level++
            nextLevelXp = (nextLevelXp * 1.3f).toInt().coerceAtLeast(5)
            pendingLevelUps.add(Unit)
        }
        if (!showCardChoice) processNextLevelUp()
    }

    fun applyGlobalSlow(multiplier: Float, durationMs: Long) {
        val now = System.currentTimeMillis()
        globalSlowMultiplier = multiplier
        globalSlowUntilMs = now + durationMs

        enemies.forEach {
            it.applySlow(multiplier, durationMs, now)
        }
    }

    fun addEnemy(enemy: BaseEnemy) {
        val now = System.currentTimeMillis()
        globalSlowMultiplier?.let { multiplier ->
            val remaining = (globalSlowUntilMs ?: now) - now
            if (remaining > 0) {
                enemy.applySlow(multiplier, remaining, now)
            }
        }
        enemies.add(enemy)
    }

    private fun processNextLevelUp() {
        if (pendingLevelUps.isNotEmpty()) {
            cardChoices = cardManager.generateThree()
            showCardChoice = true
            running = false
        }
    }

    fun applyCard(card: Card) {
        cardManager.applyCard(card)

        showCardChoice = false
        cardChoices = emptyList()

        when (card.effectType) {
            CardEffectType.ADD_TOWER -> {
                towerToPlaceOptions = unlockedTowerTypes.toList()
                towerToPlace = null
                awaitingTowerPlacement = true
                running = false
            }

            else -> {
            }
        }

        if (pendingLevelUps.isNotEmpty()) {
            pendingLevelUps.removeAt(0)
            if (pendingLevelUps.isNotEmpty()) {
                processNextLevelUp()
            }
        }

        if (!showCardChoice && !showUpgradeModal && !awaitingTowerPlacement && !placingStartingTower) {
            running = true
        }
    }


    fun upgradeCost(tower: BaseTower): Int = tower.upgradeCost(playerLevel = level)

    fun upgradeTower(tower: BaseTower) {
        val cost = tower.upgradeCost(playerLevel = level)
        if (gold >= cost) {
            gold -= cost
            tower.upgrade()
        }
    }


    fun cancelUpgrade() {
        showUpgradeModal = false
        towerToUpgrade = null
        if (!showCardChoice && !placingStartingTower && !awaitingTowerPlacement) running = true
    }

    fun upgradeTowerAt(x: Float, y: Float) {
        if (showUpgradeModal || awaitingTowerPlacement || placingStartingTower || showCardChoice) return
        val tower = towers.firstOrNull { it.isClicked(x, y) }
        if (tower != null) {
            towerToUpgrade = tower
            showUpgradeModal = true
            running = false
        }
    }
}