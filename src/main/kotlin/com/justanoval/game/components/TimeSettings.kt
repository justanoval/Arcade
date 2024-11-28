package com.justanoval.game.components

enum class TimeType {
    FORWARD, BACKWARD, INFINITE
}

data class TimeSettings(
    var length: Long,
    var type: TimeType
)