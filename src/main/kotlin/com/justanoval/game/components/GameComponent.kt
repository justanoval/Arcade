package com.justanoval.game.components

import com.justanoval.Arcade.server
import com.justanoval.game.Game
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

abstract class GameComponent {
    lateinit var game: Game
    open fun onStart(game: Game) {
        this.game = game
    }
    open fun onStop() {}
    open fun onPause() {}
    open fun onTick(delta: Int) {}
    open fun onPlayerTick(player: ServerPlayerEntity, delta: Int) {}

    fun getArg(value: Any?): Text {
        return Placeholders.parseText(Text.literal(value.toString()), PlaceholderContext.of(server))
    }

    fun getArg(value: Any?, player: ServerPlayerEntity): Text {
        return Placeholders.parseText(Text.literal(value.toString()), PlaceholderContext.of(player))
    }

    fun showTitle(title: Text? = null, subtitle: Text? = null, soundEvent: SoundEvent = SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()) {
        for (player in game.getPlayers()) {
            player.playSoundToPlayer(soundEvent, SoundCategory.MASTER, 1.0f, 1.0f)
            player.showTitle(title, subtitle, 20, 100, 20)
        }
    }

    fun playSound(soundEvent: SoundEvent, soundCategory: SoundCategory = SoundCategory.MASTER, volume: Float = 1.0f, pitch: Float = 1.0f) {
        for (player in game.getPlayers()) {
            player.playSoundToPlayer(soundEvent, soundCategory, volume, pitch)
        }
    }

    fun ServerPlayerEntity.showTitle(title: Text? = null, subtitle: Text? = null, fadeIn: Int = 20, stay: Int = 200, fadeOut: Int = 20) {
        this.networkHandler.sendPacket(TitleFadeS2CPacket(fadeIn, stay, fadeOut))
        this.networkHandler.sendPacket(TitleS2CPacket(title ?: Text.empty()))
        this.networkHandler.sendPacket(SubtitleS2CPacket(subtitle ?: Text.empty()))
    }
}