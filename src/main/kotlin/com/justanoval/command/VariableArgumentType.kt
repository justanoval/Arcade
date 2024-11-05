package com.justanoval.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext

class VariableArgumentType : ArgumentType<String> {
    companion object {
        fun variable(): VariableArgumentType {
            return VariableArgumentType()
        }

        fun getString(context: CommandContext<*>, name: String?): String {
            return context.getArgument(name, String::class.java) as String
        }
    }

    override fun parse(reader: StringReader): String {
        return reader.readString()
    }
}