package com.melongamesinc.notforgoblins.utils

object PathTemplates {
    fun basicPath(): List<Pair<Float, Float>> {
        return listOf(
            0f to 300f,
            200f to 300f,
            200f to 500f,
            500f to 500f,
            500f to 200f,
            800f to 200f,
            1000f to 400f
        )
    }
}
