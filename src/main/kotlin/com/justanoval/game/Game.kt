package com.justanoval.game

import com.justanoval.Arcade
import com.justanoval.event.ModEvent
import com.justanoval.game.components.GameComponent
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

open class Game(
    val id: String,
    val server: MinecraftServer,
    var team: Team,
    var length: Int,
    val name: String,
    private val components: List<GameComponent> = listOf()
) {
    var state: GameState = GameState.IDLE

    private var time: Int = 0
    fun getTime() = time

    private var delta: Int = 1

    val ticked = ModEvent<Int>()

    fun tick() {
        time += delta
        ticked(time)

        if (time >= length) {
            stop()
        }

        for (player in getPlayers()) {
            playerTick(player, delta)
        }

        components.forEach { c -> c.onTick(delta) }
    }

    open fun playerTick(player: ServerPlayerEntity, delta: Int) {
        components.forEach { c -> c.onPlayerTick(player, delta) }
    }

    fun start() {
        state = GameState.RUNNING
        components.forEach { c -> c.onStart(this) }

        Placeholders.register(Arcade.id(id)) { ctx, arg ->
            if (arg == null) {
                PlaceholderResult.invalid("No argument!")
            }

            when (arg) {
                "time_remaining" -> {
                    PlaceholderResult.value(formattedTime)
                }

                else -> { PlaceholderResult.invalid("Invalid argument!") }
            }
        }
    }

    fun pause() {
        state = GameState.PAUSED
        components.forEach { c -> c.onPause() }
    }

    fun stop() {
        components.forEach { c -> c.onStop() }
        state = GameState.STOPPED
    }

    fun getPlayers(): List<ServerPlayerEntity> {
        return team.playerList.mapNotNull { name ->
            server.playerManager.getPlayer(name)
        }
    }

    val formattedTime: String
        get() = getFormattedTime(getTimeRemaining())

    fun getTimeRemaining(): Int {
        return length - getTime()
    }

    fun getFormattedTime(ticks: Int): String {
        val totalSeconds = ticks / 20
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}