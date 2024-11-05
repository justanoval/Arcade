package com.justanoval.command.suggestion

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object TeamsSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for (teamName in context.source.server.scoreboard.teamNames) {
            if (CommandSource.shouldSuggest(builder.remaining, teamName)) {
                builder.suggest(teamName)
            }
        }

        return builder.buildFuture()
    }
}