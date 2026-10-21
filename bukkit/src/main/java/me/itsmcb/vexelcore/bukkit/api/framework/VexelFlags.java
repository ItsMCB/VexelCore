package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Manages a declared collection of {@link VexelFlag} definitions.
 * Provides looping, state-aware tab-completion via Brigadier's {@link SuggestionsBuilder#createOffset(int)}.
 */
public class VexelFlags {

    private final List<VexelFlag> flags;
    private final Map<String, VexelFlag> lookup = new HashMap<>();

    private VexelFlags(@NotNull List<VexelFlag> flags) {
        this.flags = List.copyOf(flags);
        for (VexelFlag f : this.flags) {
            this.lookup.put(f.getName(), f);
            for (String alias : f.getAliases()) {
                this.lookup.put(alias, f);
            }
        }
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull VexelFlags empty() {
        return builder().build();
    }

    public @NotNull List<VexelFlag> getFlags() {
        return flags;
    }

    public @Nullable VexelFlag findFlag(@NotNull String nameOrAlias) {
        String clean = nameOrAlias.replaceFirst("^-+", "").toLowerCase(Locale.ROOT);
        return lookup.get(clean);
    }

    /**
     * Converts this flag specification into an optional greedy string {@link VexelArg}
     * with looping tab-completion hooked into Paper's Brigadier engine.
     *
     * @param name        Brigadier argument node name (e.g. "flags").
     * @param description Argument description.
     */
    public @NotNull VexelArg<String> asArg(@NotNull String name, @NotNull String description) {
        return VexelArg.builder(name, StringArgumentType.greedyString(), String.class, description)
                .optional()
                .suggestsCustom(this::suggest)
                .build();
    }

    /**
     * Parses a raw flags string against this specification using {@link FlagParser}.
     */
    public @NotNull FlagParser parse(@Nullable String input) {
        return new FlagParser(input, this);
    }

    // ── Dynamic Suggestion Engine ────────────────────────────────────────

    public @NotNull CompletableFuture<Suggestions> suggest(
            @NotNull CommandContext<CommandSourceStack> ctx,
            @NotNull SuggestionsBuilder builder
    ) {
        String input = builder.getInput();
        int argStart = builder.getStart();
        String flagsInput = (argStart < input.length()) ? input.substring(argStart) : "";

        List<ParsedToken> tokens = tokenize(input, argStart);
        boolean endsWithWhitespace = flagsInput.isEmpty() || Character.isWhitespace(flagsInput.charAt(flagsInput.length() - 1));

        Set<VexelFlag> usedFlags = new HashSet<>();
        VexelFlag currentFlag = null;
        List<String> currentFlagValues = new ArrayList<>();

        int limit = endsWithWhitespace ? tokens.size() : Math.max(0, tokens.size() - 1);

        for (int i = 0; i < limit; i++) {
            ParsedToken tok = tokens.get(i);
            if (currentFlag != null) {
                currentFlagValues.add(tok.text());
                if (currentFlagValues.size() >= currentFlag.getValueCount()) {
                    if (!currentFlag.isRepeatable()) {
                        usedFlags.add(currentFlag);
                    }
                    currentFlag = null;
                    currentFlagValues.clear();
                }
            } else if (isFlagToken(tok.text())) {
                VexelFlag flag = findFlag(tok.text());
                if (flag != null) {
                    if (flag.isValued() && flag.getValueCount() > 0) {
                        currentFlag = flag;
                        currentFlagValues.clear();
                    } else {
                        if (!flag.isRepeatable()) {
                            usedFlags.add(flag);
                        }
                    }
                }
            }
        }

        if (endsWithWhitespace) {
            int cursor = input.length();
            if (currentFlag != null) {
                // Currently expecting values for currentFlag
                int valueIndex = currentFlagValues.size();
                List<String> values = currentFlag.getValueSuggestions(ctx, valueIndex, currentFlagValues);
                SuggestionsBuilder offset = builder.createOffset(cursor);
                for (String val : values) {
                    offset.suggest(quoteIfNeeded(val));
                }
                return offset.buildFuture();
            } else {
                // Loop back: suggest all remaining unused flags (and repeatable flags)
                SuggestionsBuilder offset = builder.createOffset(cursor);
                for (VexelFlag flag : flags) {
                    if (!usedFlags.contains(flag)) {
                        var tooltip = MessageComponentSerializer.message().serialize(Component.text(flag.getDescription()));
                        offset.suggest(flag.getPrefixedName(), tooltip);
                    }
                }
                return offset.buildFuture();
            }
        } else {
            // Typing an active token
            ParsedToken activeToken = tokens.get(tokens.size() - 1);
            int tokenStart = activeToken.startIndex();
            String prefix = activeToken.text();
            String prefixLower = prefix.toLowerCase(Locale.ROOT);

            if (currentFlag != null) {
                // Filtering choices for active flag value
                int valueIndex = currentFlagValues.size();
                List<String> values = currentFlag.getValueSuggestions(ctx, valueIndex, currentFlagValues);
                SuggestionsBuilder offset = builder.createOffset(tokenStart);
                for (String val : values) {
                    if (val.toLowerCase(Locale.ROOT).startsWith(prefixLower)) {
                        offset.suggest(quoteIfNeeded(val));
                    }
                }
                return offset.buildFuture();
            } else {
                // Filtering flag names
                SuggestionsBuilder offset = builder.createOffset(tokenStart);
                for (VexelFlag flag : flags) {
                    if (!usedFlags.contains(flag)) {
                        var tooltip = MessageComponentSerializer.message().serialize(Component.text(flag.getDescription()));
                        if (flag.getPrefixedName().toLowerCase(Locale.ROOT).startsWith(prefixLower)) {
                            offset.suggest(flag.getPrefixedName(), tooltip);
                        }
                        for (String aliasPrefixed : flag.getPrefixedAliases()) {
                            if (aliasPrefixed.toLowerCase(Locale.ROOT).startsWith(prefixLower)) {
                                offset.suggest(aliasPrefixed, tooltip);
                            }
                        }
                    }
                }
                return offset.buildFuture();
            }
        }
    }

    // ── Internal Helpers ─────────────────────────────────────────────────

    private record ParsedToken(@NotNull String text, int startIndex, int endIndex, boolean inQuotes) {}

    private static @NotNull List<ParsedToken> tokenize(@NotNull String input, int startOffset) {
        List<ParsedToken> tokens = new ArrayList<>();
        int i = startOffset;
        int len = input.length();

        while (i < len) {
            while (i < len && Character.isWhitespace(input.charAt(i))) {
                i++;
            }
            if (i >= len) break;

            int tokenStart = i;
            boolean inQuotes = false;
            StringBuilder sb = new StringBuilder();

            if (input.charAt(i) == '"') {
                inQuotes = true;
                i++;
                while (i < len) {
                    char c = input.charAt(i);
                    if (c == '\\' && i + 1 < len && input.charAt(i + 1) == '"') {
                        sb.append('"');
                        i += 2;
                    } else if (c == '"') {
                        i++;
                        break;
                    } else {
                        sb.append(c);
                        i++;
                    }
                }
            } else {
                while (i < len && !Character.isWhitespace(input.charAt(i))) {
                    sb.append(input.charAt(i));
                    i++;
                }
            }

            tokens.add(new ParsedToken(sb.toString(), tokenStart, i, inQuotes));
        }
        return tokens;
    }

    private static boolean isFlagToken(@Nullable String token) {
        if (token == null || token.length() <= 1 || !token.startsWith("-")) return false;
        try {
            Double.parseDouble(token);
            return false;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    private static @NotNull String quoteIfNeeded(@NotNull String val) {
        if (val.contains(" ") && !val.startsWith("\"")) {
            return "\"" + val.replace("\"", "\\\"") + "\"";
        }
        return val;
    }

    // ── Builder ──────────────────────────────────────────────────────────

    public static class Builder {
        private final List<VexelFlag> flags = new ArrayList<>();

        public Builder flag(@NotNull VexelFlag flag) {
            this.flags.add(flag);
            return this;
        }

        public @NotNull VexelFlags build() {
            return new VexelFlags(flags);
        }
    }
}