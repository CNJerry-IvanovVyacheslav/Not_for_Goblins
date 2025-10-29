package com.melongamesinc.notforgoblins.data

import android.util.Log
import com.melongamesinc.notforgoblins.domain.models.Card
import com.melongamesinc.notforgoblins.domain.models.CardEffectType
import com.melongamesinc.notforgoblins.domain.models.Tower

class CardManager(private val state: GameState) {
    var currentChoices: List<Card> = emptyList()
        internal set

    fun generateThree(): List<Card> {
        val pool = mutableListOf<Card>()
        pool.add(Card("dmg1", "Sharpened Tips", "Increase all towers' damage by 2", CardEffectType.INCREASE_TOWER_DAMAGE, 2f))
        pool.add(Card("rng1", "Extended Sights", "Increase all towers' range by 50", CardEffectType.INCREASE_TOWER_RANGE, 50f))
        pool.add(Card("gold1", "Treasure Cache", "Get 50 gold", CardEffectType.GIVE_GOLD, 50f))
        pool.add(Card("add1", "New Ballista", "Add a new basic tower on a free slot", CardEffectType.ADD_TOWER, 0f))
        pool.add(Card("dmg2", "Explosive Rounds", "Increase all towers' damage by 1", CardEffectType.INCREASE_TOWER_DAMAGE, 1f))

        val choices = pool.shuffled().take(3)
        currentChoices = choices
        Log.d("CardManager", "Generated cards: ${choices.map { it.title }}")
        return choices
    }

    fun applyCard(card: Card) {
        when (card.effectType) {
            CardEffectType.INCREASE_TOWER_DAMAGE -> {
                for (t in state.towers) t.damage += card.value.toInt()
                Log.d("CardManager", "Applied damage +${card.value.toInt()}")
            }
            CardEffectType.INCREASE_TOWER_RANGE -> {
                for (t in state.towers) t.range += card.value
                Log.d("CardManager", "Applied range +${card.value}")
            }
            CardEffectType.GIVE_GOLD -> {
                state.gold += card.value.toInt()
                Log.d("CardManager", "Gave gold ${card.value.toInt()}")
            }
            CardEffectType.ADD_TOWER -> {
                val slot = findFreeSlot()
                if (slot != null) {
                    state.towers.add(slot)
                    Log.d("CardManager", "Added new tower at (${slot.x}, ${slot.y})")
                } else {
                    // no free slots — give gold instead
                    state.gold += 25
                    Log.d("CardManager", "No free slot — gave 25 gold instead")
                }
            }
        }
    }

    private fun findFreeSlot(): Tower? {
        for (slot in state.towerSlots) {
            val occupied = state.towers.any {
                val dx = it.x - slot.first
                val dy = it.y - slot.second
                dx*dx + dy*dy < 1f
            }
            if (!occupied) {
                return Tower(slot.first, slot.second)
            }
        }
        return null
    }
}
