package com.justanoval.config

import com.google.gson.annotations.SerializedName

data class LangConfig(
    @SerializedName("events.game_started")
    val eventsGameStarted: String = "Game Started",
    @SerializedName("events.game_over")
    val eventsGameOver: String = "Game Over"
)
