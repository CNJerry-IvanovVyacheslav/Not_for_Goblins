package com.melongamesinc.notforgoblins.domain.models

data class Card(
    val id: String,
    val title: String,
    val description: String,
    val effectType: CardEffectType,
    val value: Float
)

enum class CardEffectType {
    INCREASE_TOWER_DAMAGE,
    INCREASE_TOWER_RANGE,
    GIVE_GOLD,
    ADD_TOWER
}
