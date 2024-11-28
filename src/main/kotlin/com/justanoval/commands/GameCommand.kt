package com.justanoval.commands

import com.justanoval.Arcade.logger
import com.justanoval.command.Command
import com.justanoval.command.CommandExecutor
import com.justanoval.command.SubCommand
import com.justanoval.command.Suggestion
import com.justanoval.exception.CommandException
import com.justanoval.game.Game
import com.justanoval.game.GameFactory
import com.justanoval.game.GameRegistry
import net.minecraft.scoreboard.Team
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object GameCommand : Command("game") {
    override fun getRequirements(source: ServerCommandSource): Boolean {
        return source.hasPermissionLevel(2)
    }

    @CommandExecutor
    @SubCommand("start")
    fun start(
        source: ServerCommandSource,
        @Suggestion("arcade:games")
        game: GameFactory,
        @Suggestion("arcade:teams")
        team: Team,
        length: Int = -1,
    ): Boolean {
        try {
            var builder: GameFactory = game.setTeam(team)

            if (length > 0L) {
                builder = builder.setLength(length)
            }

            val instance: Game = builder.build()

            instance.start()

            logger.info("Started game ${game.id} with team ${team.name} for $length ticks.")
            return success(source, "Started game ${instance.id} with team ${team.name} for $length ticks.")
        } catch (e: CommandException) {
            error(source, e.message ?: "There was an undocumented error: ${e::class.simpleName}.")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    @CommandExecutor
    @SubCommand("stop")
    fun stop(
        source: ServerCommandSource,
        @Suggestion("arcade:active_games") index: Int
    ): Boolean {
        val game: Game? = GameRegistry.instances.getOrNull(index)

        if (game == null) {
            source.player?.sendMessage(Text.of("There are no instances with that index"))
            return false
        }

        game.stop()
        return success(source, "Stopped game")
    }

    @CommandExecutor
    @SubCommand("list")
    fun list(source: ServerCommandSource): Boolean {
        var message = ""

        for ((index, instance) in GameRegistry.instances.withIndex()) {
            message += "- $index: ${instance::class.simpleName}\n"
        }

        if (GameRegistry.instances.size <= 0) {
            return error(source, "No game instances were found.")
        }

        return success(source, message)
    }
}