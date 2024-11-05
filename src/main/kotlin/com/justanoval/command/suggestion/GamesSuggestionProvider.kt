package com.justanoval.command.suggestion

import com.justanoval.game.GameRegistry
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object GamesSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for (gameId in GameRegistry.items.map { game -> game.key.path }) {
            if (CommandSource.shouldSuggest(builder.remaining, gameId)) {
                builder.suggest(gameId)
            }
        }

        return builder.buildFuture()
    }
}