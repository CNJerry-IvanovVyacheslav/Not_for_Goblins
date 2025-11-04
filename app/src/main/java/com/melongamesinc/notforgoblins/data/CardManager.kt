package com.melongamesinc.notforgoblins.data

import com.melongamesinc.notforgoblins.domain.models.Card
import com.melongamesinc.notforgoblins.domain.models.CardEffectType

class CardManager(private val state: GameState) {

    var currentChoices: List<Card> = emptyList()
        internal set

    fun generateThree(): List<Card> {
        val pool = mutableListOf<Card>()

        pool.add(
            Card(
                "dmg1",
                "Sharpened Tips",
                "Increase all towers' damage by 1",
                CardEffectType.INCREASE_TOWER_DAMAGE,
                1f
            )
        )
        pool.add(
            Card(
                "rng1",
                "Extended Sights",
                "Increase all towers' range by 5",
                CardEffectType.INCREASE_TOWER_RANGE,
                5f
            )
        )
        pool.add(Card("gold1", "Treasure Cache", "Get 500 gold", CardEffectType.GIVE_GOLD, 500f))
        pool.add(
            Card(
                "add1",
                "New Tower",
                "Add a new tower on a free slot",
                CardEffectType.ADD_TOWER
            )
        )

        pool.add(
            Card(
                "unlock_mortar",
                "Unlock Mortar",
                "Unlocks Splash Tower",
                CardEffectType.UNLOCK_TOWER,
                payload = "SPLASH"
            )
        )
        pool.add(
            Card(
                "unlock_sniper",
                "Unlock Sniper",
                "Unlocks Sniper Tower",
                CardEffectType.UNLOCK_TOWER,
                payload = "SNIPER"
            )
        )
        pool.add(
            Card(
                "unlock_slow",
                "Unlock Slow Tower",
                "Unlocks Slow Tower",
                CardEffectType.UNLOCK_TOWER,
                payload = "SLOW"
            )
        )

        pool.add(
            Card(
                "one_shot",
                "Cleansing Flame",
                "Deal 50 damage to all enemies",
                CardEffectType.ONE_SHOT_DAMAGE,
                50f
            )
        )
        pool.add(
            Card(
                "stun",
                "Bell of Panic",
                "Stun all enemies for 3 seconds",
                CardEffectType.STUN_ALL,
                3000f
            )
        )

        pool.add(
            Card(
                "proj_speed",
                "Quick Arrows",
                "Increase all towers' attack speed by 5%",
                CardEffectType.GLOBAL_BUFF,
                1.05f
            )
        )
        pool.add(
            Card(
                "gold_bonus",
                "Gold Rush",
                "Increase gold reward from each enemy by 1",
                CardEffectType.GLOBAL_BUFF,
                1f
            )
        )
        pool.add(
            Card(
                "xp_bonus",
                "Knowledge Tome",
                "Increase XP from each enemy by 2",
                CardEffectType.GLOBAL_BUFF,
                2f
            )
        )
        pool.add(
            Card(
                "instant_xp",
                "Knowledge Crystal",
                "Gain 100 XP instantly",
                CardEffectType.GLOBAL_BUFF,
                value = 100f
            )
        )
        pool.add(
            Card(
                "crit_chance",
                "Sharpened Arrows",
                "All towers gain 10% critical hit chance",
                CardEffectType.GLOBAL_BUFF,
                0.1f
            )
        )
        pool.add(
            Card(
                "range2",
                "Far Sighted",
                "Increase all towers' range by 10",
                CardEffectType.INCREASE_TOWER_RANGE,
                10f
            )
        )
        pool.add(
            Card(
                "dmg2",
                "Deadly Tips",
                "Increase all towers' damage by 2",
                CardEffectType.INCREASE_TOWER_DAMAGE,
                2f
            )
        )
        pool.add(Card("gold2", "Treasure Hoard", "Get 1000 gold", CardEffectType.GIVE_GOLD, 1000f))
        pool.add(
            Card(
                "heal_base",
                "Reinforced Walls",
                "Restore 5 base health",
                CardEffectType.GLOBAL_BUFF,
                5f
            )
        )
        pool.add(
            Card(
                "slow_all",
                "Icy Mist",
                "Slow all enemies by 50% for 10 seconds",
                CardEffectType.GLOBAL_BUFF,
                10000f
            )
        )
        pool.add(
            Card(
                "add_upgrade_tower",
                "Upgrade Tower",
                "Upgrade a random tower for free",
                CardEffectType.UPGRADE_RANDOM
            )
        )

        val filteredPool = pool.filter { card ->
            when (card.effectType) {
                CardEffectType.UNLOCK_TOWER -> card.payload?.let { it !in state.unlockedTowerTypes }
                    ?: true

                CardEffectType.ADD_TOWER -> state.towerSlots.any {
                    state.isSlotFree(
                        state.towerSlots.indexOf(
                            it
                        )
                    )
                }

                else -> true
            }
        }

        currentChoices = filteredPool.shuffled().take(3)
        return currentChoices
    }

    fun reset() {
        currentChoices = emptyList()
    }

    fun applyCard(card: Card) {
        when (card.effectType) {
            CardEffectType.INCREASE_TOWER_DAMAGE -> {
                state.towers.forEach { it.damage += card.value.toInt() }
                state.globalDamageBonus += card.value.toInt()
            }

            CardEffectType.INCREASE_TOWER_RANGE -> {
                state.towers.forEach { it.range += card.value }
                state.globalRangeBonus += card.value
            }

            CardEffectType.GIVE_GOLD -> state.gold += (card.value * state.goldMultiplier).toInt()

            CardEffectType.ADD_TOWER -> {
            }

            CardEffectType.UNLOCK_TOWER -> card.payload?.let { state.unlockedTowerTypes.add(it) }

            CardEffectType.ONE_SHOT_DAMAGE -> state.enemies.forEach { it.hp -= card.value.toInt() }

            CardEffectType.STUN_ALL -> {
                val now = System.currentTimeMillis()
                state.enemies.forEach { it.applyStun(card.value.toLong(), now) }
            }

            CardEffectType.GLOBAL_BUFF -> {
                when (card.id) {
                    "proj_speed" -> {
                        state.towers.forEach { it.fireRate *= card.value }
                        state.globalFireRateMultiplier *= card.value
                    }

                    "gold_bonus" -> state.goldMultiplier += card.value
                    "xp_bonus" -> state.xpMultiplier += card.value
                    "instant_xp" -> {
                        state.experience += card.value.toInt()
                        state.checkLevelUp()
                    }

                    "crit_chance" -> state.globalCritChance =
                        (state.globalCritChance ?: 0f) + card.value

                    "heal_base" -> state.baseHealth += card.value.toInt()
                    "slow_all" -> {
                        val duration = card.value.toLong()
                        state.applyGlobalSlow(0.5f, duration)
                    }
                }
            }

            CardEffectType.UPGRADE_RANDOM -> {
                val tower = state.towers.randomOrNull()
                tower?.upgradeFree()
            }
        }
    }
}