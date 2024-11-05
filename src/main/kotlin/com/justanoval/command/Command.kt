package com.justanoval.command

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

abstract class Command(val name: String) {
    abstract fun getRequirements(source: ServerCommandSource): Boolean

    fun build(): LiteralArgumentBuilder<ServerCommandSource> {
        var literal = CommandManager.literal(name)
        val mainExecutor = getMainExecutor()

        // Build main literal
        if (mainExecutor != null) {
            literal = literal.then(buildExecutor(mainExecutor))
        }

        // Add sub literals
        for ((name, function) in getSubExecutors()) {
            val executor = buildExecutor(function)
            literal = if (executor != null) {
                literal.then(CommandManager.literal(name).then(executor))
            } else {
                literal.then(CommandManager.literal(name).executes { context -> execute(context, null) })
            }
        }

        // Set requirements
        literal = literal.requires {source -> getRequirements(source) }

        return literal
    }

    private fun getSuggestion(parameter: KParameter): Suggestion? {
        return parameter.findAnnotation<Suggestion>()
    }

    private fun buildExecutor(executor: KFunction<*>): ArgumentBuilder<ServerCommandSource, *>? {
        val parameters = getParameters(executor)
        val args = parameters.associate { it.name!! to it.type }
        var argumentBuilder: ArgumentBuilder<ServerCommandSource, *>? = null

        for ((index, parameter) in parameters.reversed().withIndex()) {
            var argument = CommandManager.argument(parameter.name, getArgumentType(parameter.type))

            val suggestion = getSuggestion(parameter)
            if (suggestion != null) {
                val identifier = Identifier.of(suggestion.provider)
                argument = argument.suggests(CommandRegistry.getSuggestionProvider(identifier))
            }

            // Add execution for optional and final arguments
            if (parameter.isOptional || index == 0) {
                argument = argument.executes { context -> execute(context, args) }
            }

            argumentBuilder = if (argumentBuilder == null) {
                argument
            } else {
                argument.then(argumentBuilder)
            }
        }

        return argumentBuilder
    }

    private fun getExecutors(): List<KFunction<*>> {
        return this::class.functions.filter { function -> function.hasAnnotation<CommandExecutor>() }
    }

    private fun getMainExecutor(): KFunction<*>? {
        val executors = getExecutors().filter { function -> !function.hasAnnotation<SubCommand>() }

        if (executors.size > 1) {
            return executors[0]
        }

        return null
    }

    private fun getSubExecutors(): Map<String, KFunction<*>> {
        return getExecutors()
            .filter { function -> function.hasAnnotation<SubCommand>() }
            .flatMap { function ->
                val subCommand = function.findAnnotation<SubCommand>()
                subCommand?.names?.map { name -> name to function } ?: emptyList()
            }.toMap()
    }

    private fun getParameters(function: KFunction<*>): List<KParameter> {
        return function.parameters.drop(2).filter { parameter -> parameter.name != null }
    }

    private fun getArgumentType(type: KType): ArgumentType<*>? {
        return when (type.classifier) {
            String::class -> StringArgumentType.string()
            Int::class -> IntegerArgumentType.integer()
            Boolean::class -> BoolArgumentType.bool()
            Double::class -> DoubleArgumentType.doubleArg()
            Float::class -> FloatArgumentType.floatArg()
            else -> StringArgumentType.string()
        }
    }

    private fun interpretArgument(name: String?, type: KType, context: CommandContext<ServerCommandSource>): Any? {
        var argument: Any? = when (type.classifier) {
            String::class -> StringArgumentType.getString(context, name)
            Int::class -> IntegerArgumentType.getInteger(context, name)
            Boolean::class -> BoolArgumentType.getBool(context, name)
            Double::class -> DoubleArgumentType.getDouble(context, name)
            Float::class -> FloatArgumentType.getFloat(context, name)
            else -> null
        }

        if (argument == null && name != null && type.classifier != null) {
            val string = StringArgumentType.getString(context, name)
            argument = CommandRegistry.interpretArgument(type.classifier!!, string)
        }

        return argument
    }

    private fun execute(context: CommandContext<ServerCommandSource>, args: Map<String, KType>?): Int {
        val literals = context.nodes
            .filter { node -> node.node is LiteralCommandNode<*> }
            .map { node -> node.node.name }
            .toTypedArray()

        val commandFunction = findExecutor(*literals.copyOfRange(1, literals.size))

        if (commandFunction != null) {
            try {
                val execution = commandFunction(context, args)

                try {
                    return if (execution) 1 else 0
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return 0
            } catch (e: Exception) {
                e.message?.let { error(context.source, it) }
            }
        }

        return 0
    }

    private fun findExecutor(vararg subcommands: String): ((CommandContext<ServerCommandSource>, Map<String, KType>?) -> Boolean)? {
        val commandFunctions = this::class.functions

        for (function in commandFunctions) {
            val commandAnnotation = function.findAnnotation<CommandExecutor>()
            if (commandAnnotation != null) {
                val subCommandAnnotation = function.findAnnotation<SubCommand>()
                if (subCommandAnnotation != null && subCommandAnnotation.names.contentEquals(subcommands)) {
                    return { context: CommandContext<ServerCommandSource>, args: Map<String, KType>? ->
                        if (function.visibility != KVisibility.PUBLIC) {
                            throw Exception("Command function \"${function.name}\" must be public.")
                        }

                        try {
                            if (args != null) {
                                function.call(this, context.source, *args.map { (name, type) -> interpretArgument(name, type, context) }.toTypedArray()) as Boolean
                            } else {
                                function.call(this, context.source) as Boolean
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        false
                    }
                }
            }
        }

        return null
    }

    fun error(source: ServerCommandSource, message: String): Boolean {
        source.sendMessage(Text.of(message).getWithStyle(Style.EMPTY.withColor(Colors.LIGHT_RED)).component1())
        if (source.isExecutedByPlayer) {
            source.player?.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value())
        }
        return false
    }

    fun success(source: ServerCommandSource, message: String): Boolean {
        source.sendMessage(Text.of(message))
        return true
    }
}