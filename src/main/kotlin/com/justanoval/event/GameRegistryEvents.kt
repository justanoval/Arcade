package com.justanoval.event

import com.justanoval.event.GameRegistryEvents.GamesRegistered
import com.justanoval.game.GameRegistry
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.MinecraftServer

object GameRegistryEvents {
    val GAMES_REGISTERED: Event<GamesRegistered> = EventFactory.createArrayBacked(
        GamesRegistered::class.java
    ) { callbacks: Array<GamesRegistered> ->
        GamesRegistered { gameRegistry: GameRegistry?, server: MinecraftServer? ->
            for (callback in callbacks) {
                callback.onGamesRegistered(gameRegistry, server)
            }
        }
    }

    fun interface GamesRegistered {
        fun onGamesRegistered(gameRegistry: GameRegistry?, server: MinecraftServer?)
    }
}