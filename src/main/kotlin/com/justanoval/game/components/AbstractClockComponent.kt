package com.justanoval.game.components

import net.minecraft.sound.SoundEvents

abstract class AbstractClockComponent : GameComponent() {
    override fun onTick(delta: Int) {
        super.onTick(delta)

        val time = game.getTimeRemaining()

        if (time % 1200 == 0) {
            this.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), volume = 0.5f)
        }
    }
}