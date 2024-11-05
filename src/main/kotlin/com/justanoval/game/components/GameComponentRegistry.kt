package com.justanoval.game.components

import com.justanoval.registry.Registry
import kotlin.reflect.KClass

object GameComponentRegistry : Registry<KClass<out GameComponent>>()