package com.justanoval.game.components

import com.justanoval.Arcade
import com.justanoval.config.FileLoader
import net.minecraft.util.Identifier
import java.io.File
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

object ComponentScriptLoader {
    val engine: ScriptEngine? = ScriptEngineManager().getEngineByExtension("kts")
    val components: MutableMap<Identifier, GameComponent> = mutableMapOf()

    init {
        if (engine != null) {
            Arcade.logger.info("Kotlin script engine found!")
            val files = FileLoader.getFiles("components")
            if (files != null) {
                for (file in files) {
                    val component = loadComponent(file)
                    if (component != null) {
                        components[Arcade.id(component::class.simpleName!!)] = component
                    }
                }
            }
        } else {
            Arcade.logger.warn("Kotlin script engine not found!")
        }
    }

    private fun loadComponent(file: File): GameComponent? {
        if (file.isFile) {
            val obj = engine?.eval(file.reader()) as? GameComponent
            if (obj is GameComponent && obj::class.simpleName != null) {
                return obj
            } else {
                Arcade.logger.warn("Could not load script: ${file.path}")
            }
        }

        return null
    }
}