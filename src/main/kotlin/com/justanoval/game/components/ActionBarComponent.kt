package com.justanoval.game.components

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ActionBarComponent(
    val format: String = ""
) : AbstractClockComponent() {
    override fun onStop() {
        for (player in game.getPlayers()) {
            player.sendMessage(Text.empty(), true)
        }
    }

    override fun onPlayerTick(player: ServerPlayerEntity, delta: Int) {
        player.sendMessage(Text.literal(format), true)
    }
}