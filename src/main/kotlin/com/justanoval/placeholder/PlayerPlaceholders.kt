package com.justanoval.placeholder

import com.justanoval.game.Game
import com.justanoval.game.GameRegistry
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object PlayerPlaceholders {
    init {
        Placeholders.register(Identifier.of("player", "game")) { ctx, arg ->
            val player = ctx.player ?: return@register PlaceholderResult.invalid("Invalid context")
            val game = player.getCurrentGame()

            if (arg == null) {
                PlaceholderResult.value(game.id)
            }

            when (arg) {
                "name" -> PlaceholderResult.value(game.name)
                "time_remaining" -> PlaceholderResult.value(game.formattedTime)

                else -> { PlaceholderResult.invalid("Invalid argument!") }
            }
        }
    }

    fun ServerPlayerEntity.getCurrentGame(): Game {
        return GameRegistry.instances.filter { game -> game.getPlayers().contains(this) }.component1()
    }
}