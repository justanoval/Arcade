package com.justanoval.command.suggestion

import com.justanoval.game.GameRegistry
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object ActiveGamesSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for ((index, _) in GameRegistry.instances.withIndex()) {
            if (CommandSource.shouldSuggest(builder.remaining, index.toString())) {
                builder.suggest(index.toString())
            }
        }

        return builder.buildFuture()
    }
}