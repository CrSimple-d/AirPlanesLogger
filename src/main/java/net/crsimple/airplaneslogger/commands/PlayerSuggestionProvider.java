package net.crsimple.airplaneslogger.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        var server = context.getSource();
        Collection<String> playerNames = server.getOnlinePlayerNames();
        for (var name : playerNames) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    }
}
