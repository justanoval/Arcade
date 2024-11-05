package com.justanoval.command

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import kotlin.reflect.KClassifier

object CommandRegistry {
    private val suggestionProviders: MutableMap<Identifier, SuggestionProvider<ServerCommandSource>> = mutableMapOf()
    private val argumentInterpreters: MutableMap<KClassifier, ArgumentInterpreter<String, *>> = mutableMapOf()

    fun interpretArgument(classifier: KClassifier, value: String): Any? {
        return argumentInterpreters[classifier]?.interpret(value)
    }

    fun registerArgumentInterpreter(kClass: KClassifier, interpreter: ArgumentInterpreter<String, Any?>) {
        argumentInterpreters[kClass] = interpreter
    }

    fun registerSuggestionProvider(id: Identifier, provider: SuggestionProvider<ServerCommandSource>) {
        suggestionProviders[id] = provider
    }

    fun getSuggestionProvider(id: Identifier): SuggestionProvider<ServerCommandSource>? {
        return suggestionProviders[id]
    }

    fun registerCommand(command: Command) {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(command.build())
        }
    }
}