package com.justanoval.game.components

import com.justanoval.game.Game
import eu.pb4.sidebars.api.Sidebar
import eu.pb4.sidebars.api.SidebarUtils
import net.minecraft.server.network.ServerPlayerEntity

class SidebarComponent(
    val title: String = "",
    val lines: List<String> = listOf()
) : AbstractClockComponent() {
    private lateinit var sidebar: Sidebar

    override fun onStart(game: Game) {
        super.onStart(game)
        this.sidebar = Sidebar(Sidebar.Priority.MEDIUM)

        for (player in game.getPlayers()) {
            sidebar.addPlayer(player)
        }

        sidebar.show()
    }

    override fun onStop() {
        sidebar.clearLines()
        sidebar.hide()
        for (player in game.getPlayers()) {
            sidebar.removePlayer(player)
        }
    }

    override fun onPlayerTick(player: ServerPlayerEntity, delta: Int) {
        if (!sidebar.playerHandlerSet.contains(player.networkHandler)) {
            sidebar.addPlayer(player)
            SidebarUtils.addSidebar(player.networkHandler, sidebar)
        } else {
            sidebar.title = getArg(title, player)
            for ((index, string) in lines.withIndex()) {
                if (index >= 15) break
                sidebar.setLine(index, getArg(string, player))
            }
        }
    }
}