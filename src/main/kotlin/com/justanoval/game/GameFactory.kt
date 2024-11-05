package com.justanoval.game

import com.justanoval.event.ModEvent
import com.justanoval.exception.CommandException
import com.justanoval.game.components.GameComponent
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer

class GameFactory(
    val id: String,
    val server: MinecraftServer,
    val name: String = "Default",
    val components: List<GameComponent>
) {

    val instanceCreated = ModEvent<Game>()

    private var team: Team? = null
    private var length: Int = 0
    private var count: Int = 0

    fun setTeam(team: Team): GameFactory {
        this.team = team
        return this
    }

    fun setLength(length: Int): GameFactory {
        this.length = length
        return this
    }

    fun build(): Game? {
        if (team == null) {
            throw CommandException("Invalid or null team.")
        }

        if (length <= 0) {
            throw CommandException("Length must be greater than 0.")
        }

        val instance = Game("$id-$count", server, team!!, length, name, components)

        instanceCreated(instance)

        return instance
    }
}