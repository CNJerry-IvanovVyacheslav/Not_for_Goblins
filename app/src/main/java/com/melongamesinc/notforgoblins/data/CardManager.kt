package com.melongamesinc.notforgoblins.data

import android.util.Log
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
                "Increase all towers' damage by 2",
                CardEffectType.INCREASE_TOWER_DAMAGE,
                2f
            )
        )
        pool.add(
            Card(
                "rng1",
                "Extended Sights",
                "Increase all towers' range by 50",
                CardEffectType.INCREASE_TOWER_RANGE,
                50f
            )
        )
        pool.add(Card("gold1", "Treasure Cache", "Get 50 gold", CardEffectType.GIVE_GOLD, 50f))
        pool.add(
            Card(
                "add1",
                "New Ballista",
                "Add a new basic tower on a free slot",
                CardEffectType.ADD_TOWER
            )
        )
        pool.add(
            Card(
                "unlock_mortar",
                "Unlock Mortar",
                "Unlocks the Mortar (SplashTower)",
                CardEffectType.UNLOCK_TOWER,
                payload = "SPLASH"
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
                "Stun all enemies for 1500ms",
                CardEffectType.STUN_ALL,
                1500f
            )
        )
        pool.add(
            Card(
                "proj_speed",
                "Quick Arrows",
                "Increase all towers' attack speed by 50%",
                CardEffectType.GLOBAL_BUFF,
                1.5f
            )
        )

        val filteredPool = pool.filter { card ->
            when (card.effectType) {
                CardEffectType.UNLOCK_TOWER -> card.payload?.let { it !in state.unlockedTowerTypes }
                    ?: true

                CardEffectType.ADD_TOWER -> state.awaitingTowerPlacement.not()
                else -> true
            }
        }

        currentChoices = filteredPool.shuffled().take(3)
        return currentChoices
    }

    fun applyCard(card: Card) {
        when (card.effectType) {
            CardEffectType.INCREASE_TOWER_DAMAGE ->
                state.towers.forEach { it.damage += card.value.toInt() }

            CardEffectType.INCREASE_TOWER_RANGE ->
                state.towers.forEach { it.range += card.value }

            CardEffectType.GIVE_GOLD ->
                state.gold += card.value.toInt()

            CardEffectType.ADD_TOWER ->
                state.awaitingTowerPlacement = true

            CardEffectType.UNLOCK_TOWER ->
                card.payload?.let { state.unlockedTowerTypes.add(it) }

            CardEffectType.ONE_SHOT_DAMAGE ->
                state.enemies.forEach { it.hp -= card.value.toInt() }

            CardEffectType.STUN_ALL -> {
                val now = System.currentTimeMillis()
                state.enemies.forEach { it.applySlow(0f, card.value.toLong(), now) }
            }

            CardEffectType.GLOBAL_BUFF ->
                state.towers.forEach { it.fireRate *= card.value }

            else -> Unit
        }
        Log.d("CardManager", "Applied ${card.title}")
    }
}
