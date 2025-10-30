package com.melongamesinc.notforgoblins.domain.models

data class Card(
    val id: String,
    val title: String,
    val description: String,
    val effectType: CardEffectType,
    val value: Float = 0f,
    val payload: String? = null
)

enum class CardEffectType {
    INCREASE_TOWER_DAMAGE,
    INCREASE_TOWER_RANGE,
    GIVE_GOLD,
    ADD_TOWER,
    UNLOCK_TOWER,
    GLOBAL_BUFF,
    ONE_SHOT_DAMAGE,
    STUN_ALL
}