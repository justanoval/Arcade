package com.justanoval.game

import com.justanoval.Arcade
import com.justanoval.registry.Registry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.util.Identifier

object GameRegistry : Registry<GameFactory>() {
    val instances: MutableList<Game> = mutableListOf()

    init {
        ServerTickEvents.START_SERVER_TICK.register { server ->
            if (!server.tickManager.isFrozen) {
                tick()
            }
        }
    }

    fun register(item: GameFactory) {
        this.register(Arcade.id(item.id), item)
    }

    override fun register(id: Identifier, item: GameFactory) {
        super.register(id, item)

        item.instanceCreated += { instance ->
            instances.add(instance)
        }
    }

    private fun tick() {
        val iterator = instances.iterator()
        while (iterator.hasNext()) {
            val game = iterator.next()
            if (game.state == GameState.RUNNING) {
                game.tick()
            } else if (game.state == GameState.STOPPED) {
                iterator.remove()
            }
        }
    }
}