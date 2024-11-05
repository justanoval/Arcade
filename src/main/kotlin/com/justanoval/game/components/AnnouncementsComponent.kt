package com.justanoval.game.components

import com.justanoval.Arcade
import com.justanoval.game.Game
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

class AnnouncementsComponent(
    @SerializedName("lang.game_started")
    val gameStarted: String = Arcade.lang.eventsGameStarted,
    @SerializedName("lang.game_over")
    val gameOver: String = Arcade.lang.eventsGameOver,
    @SerializedName("sound.game_started")
    val soundGameStarted: String = "minecraft:block.note_block.pling",
    @SerializedName("sound.game_over")
    val soundGameOver: String = "minecraft:block.note_block.pling"
): GameComponent() {
    override fun onStart(game: Game) {
        super.onStart(game)
        this.game = game
        showTitle(subtitle = getArg(gameStarted), soundEvent = SoundEvent.of(Identifier.of(soundGameStarted)))
    }

    override fun onStop() {
        super.onStop()
        showTitle(subtitle = getArg(gameOver), soundEvent = SoundEvent.of(Identifier.of(soundGameOver)))
    }
}