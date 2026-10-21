package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a typed, buildable command argument with robust suggestion capabilities.
 * Uses the Builder pattern to avoid telescoping constructors.
 *
 * <p>Convenience factory methods are provided for common types:</p>
 * <pre>{@code
 * VexelArg.string("name", "World name")
 * VexelArg.integer("count", "Number of items")
 * VexelArg.choice("type", "World type", List.of("normal", "flat"))
 * }</pre>
 *
 * <p>For more control, use the builder:</p>
 * <pre>{@code
 * VexelArg.<String>builder("name", StringArgumentType.string(), String.class, "World name")
 *     .suggests(() -> worldManager.getWorldNames())
 *     .suggestionMode(VexelArg.SuggestionMode.NAMESPACED_KEY)
 *     .optional()
 *     .build();
 * }</pre>
 *
 * @param <T> The Java type of the argument value.
 */
public class VexelArg<T> {

    private final String name;
    private final ArgumentType<T> type;
    private final Class<T> typeClass;
    private final String description;
    private final boolean optional;
    private final SuggestionMode suggestionMode;

    @Nullable private final List<String> staticChoices;
    @Nullable private final Supplier<List<String>> dynamicChoices;
    @Nullable private final Function<CommandContext<CommandSourceStack>, List<String>> contextualChoices;
    @Nullable private final Function<CommandContext<CommandSourceStack>, CompletableFuture<List<String>>> asyncChoices;
    @Nullable private final SuggestionProvider<CommandSourceStack> customSuggestionProvider;

    /**
     * Defines how suggestions are filtered and matched against user input.
     */
    public enum SuggestionMode {
        /** Standard string startsWith matching. */
        SIMPLE,
        /** Matches both the full key and the key-after-colon (e.g., 'minecraft:overworld' matches 'over'). */
        NAMESPACED_KEY
    }

    private VexelArg(Builder<T> builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.typeClass = builder.typeClass;
        this.description = builder.description;
        this.optional = builder.optional;
        this.suggestionMode = builder.suggestionMode;
        this.staticChoices = builder.staticChoices;
        this.dynamicChoices = builder.dynamicChoices;
        this.contextualChoices = builder.contextualChoices;
        this.asyncChoices = builder.asyncChoices;
        this.customSuggestionProvider = builder.customSuggestionProvider;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public @NotNull String getName() { return name; }
    public @NotNull Class<T> getTypeClass() { return typeClass; }
    public @NotNull String getDescription() { return description; }
    public boolean isOptional() { return optional; }

    // ── Convenience factory methods ──────────────────────────────────────

    /** Creates a {@link StringArgumentType#string()} argument (quoted, allows spaces). */
    public static VexelArg<String> string(@NotNull String name, @NotNull String description) {
        return of(name, StringArgumentType.string(), String.class, description);
    }

    /** Creates a {@link StringArgumentType#word()} argument (single word, no spaces). */
    public static VexelArg<String> word(@NotNull String name, @NotNull String description) {
        return of(name, StringArgumentType.word(), String.class, description);
    }

    /** Creates a {@link StringArgumentType#greedyString()} argument (consumes all remaining input). */
    public static VexelArg<String> greedyString(@NotNull String name, @NotNull String description) {
        return of(name, StringArgumentType.greedyString(), String.class, description);
    }

    /** Creates an {@link IntegerArgumentType#integer()} argument. */
    public static VexelArg<Integer> integer(@NotNull String name, @NotNull String description) {
        return of(name, IntegerArgumentType.integer(), Integer.class, description);
    }

    /** Creates a string argument with a fixed set of suggestions. */
    public static VexelArg<String> choice(@NotNull String name, @NotNull String description, @NotNull List<String> choices) {
        return builder(name, StringArgumentType.string(), String.class, description)
                .suggests(choices)
                .build();
    }

    /** Quick-build shortcut for the common case. */
    public static <T> VexelArg<T> of(@NotNull String name, @NotNull ArgumentType<T> type, @NotNull Class<T> typeClass, @NotNull String description) {
        return builder(name, type, typeClass, description).build();
    }

    /** Creates a word argument (no spaces) with built-in tab completion for currently online players. */
    public static VexelArg<String> onlinePlayer(@NotNull String name, @NotNull String description) {
        return builder(name, StringArgumentType.word(), String.class, description)
                .suggests(() -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList())
                .build();
    }

    /** Creates a string argument (allows spaces) with built-in tab completion for loaded worlds. */
    public static VexelArg<String> world(@NotNull String name, @NotNull String description) {
        return builder(name, StringArgumentType.string(), String.class, description)
                .suggests(() -> Bukkit.getWorlds().stream().map(World::getName).toList())
                .build();
    }

    /** Creates an optional flags argument with looping tab-completion. */
    public static VexelArg<String> flags(@NotNull String name, @NotNull String description, @NotNull VexelFlags flags) {
        return flags.asArg(name, description);
    }

    // ── Builder entry point ──────────────────────────────────────────────

    /**
     * Creates a builder for a VexelArg.
     *
     * @param name        The name of the argument (used for Brigadier node + retrieval).
     * @param type        The Brigadier {@link ArgumentType}.
     * @param typeClass   The raw class of the type (required for type-safe retrieval via {@code ctx.getArgument}).
     * @param description The description for hover tooltips and help text.
     */
    public static <T> Builder<T> builder(@NotNull String name, @NotNull ArgumentType<T> type, @NotNull Class<T> typeClass, @NotNull String description) {
        return new Builder<>(name, type, typeClass, description);
    }

    // ── Brigadier conversion ─────────────────────────────────────────────

    public RequiredArgumentBuilder<CommandSourceStack, T> toBrigadier() {
        RequiredArgumentBuilder<CommandSourceStack, T> builder = Commands.argument(this.name, this.type);
        var tooltip = MessageComponentSerializer.message().serialize(Component.text(this.description));

        if (customSuggestionProvider != null) {
            builder.suggests(customSuggestionProvider);
        } else if (asyncChoices != null) {
            builder.suggests((ctx, sb) ->
                    asyncChoices.apply(ctx).thenCompose(choices -> filter(sb, choices, tooltip, suggestionMode)));
        } else if (contextualChoices != null) {
            builder.suggests((ctx, sb) ->
                    filter(sb, contextualChoices.apply(ctx), tooltip, suggestionMode));
        } else if (dynamicChoices != null) {
            builder.suggests((ctx, sb) ->
                    filter(sb, dynamicChoices.get(), tooltip, suggestionMode));
        } else if (staticChoices != null && !staticChoices.isEmpty()) {
            builder.suggests((ctx, sb) ->
                    filter(sb, staticChoices, tooltip, suggestionMode));
        }

        return builder;
    }

    /**
     * Centralized, namespace-aware suggestion filtering logic.
     */
    private static CompletableFuture<Suggestions> filter(
            @NotNull SuggestionsBuilder builder,
            @Nullable List<String> choices,
            @Nullable com.mojang.brigadier.Message tooltip,
            @NotNull SuggestionMode mode) {

        if (choices != null) {
            String typed = builder.getRemainingLowerCase();
            for (String choice : choices) {
                String lower = choice.toLowerCase(Locale.ROOT);
                boolean matches;
                if (mode == SuggestionMode.NAMESPACED_KEY && lower.contains(":")) {
                    matches = lower.startsWith(typed)
                            || lower.substring(lower.indexOf(':') + 1).startsWith(typed);
                } else {
                    matches = lower.startsWith(typed);
                }
                if (matches) {
                    builder.suggest(choice, tooltip);
                }
            }
        }
        return builder.buildFuture();
    }

    // ── Builder ──────────────────────────────────────────────────────────

    public static class Builder<T> {
        private final String name;
        private final ArgumentType<T> type;
        private final Class<T> typeClass;
        private final String description;

        private boolean optional = false;
        private SuggestionMode suggestionMode = SuggestionMode.SIMPLE;

        @Nullable private List<String> staticChoices;
        @Nullable private Supplier<List<String>> dynamicChoices;
        @Nullable private Function<CommandContext<CommandSourceStack>, List<String>> contextualChoices;
        @Nullable private Function<CommandContext<CommandSourceStack>, CompletableFuture<List<String>>> asyncChoices;
        @Nullable private SuggestionProvider<CommandSourceStack> customSuggestionProvider;

        private Builder(@NotNull String name, @NotNull ArgumentType<T> type, @NotNull Class<T> typeClass, @NotNull String description) {
            this.name = name;
            this.type = type;
            this.typeClass = typeClass;
            this.description = description;
        }

        // ── Configuration ────────────────────────────────────────────────

        /** Marks this argument as optional. Optional arguments must come after required ones. */
        public Builder<T> optional() { this.optional = true; return this; }

        /** Explicitly sets whether this argument is optional. */
        public Builder<T> optional(boolean optional) { this.optional = optional; return this; }

        /** Sets the suggestion filtering mode. */
        public Builder<T> suggestionMode(@NotNull SuggestionMode mode) { this.suggestionMode = mode; return this; }

        /** Provides a static list of suggestions. */
        public Builder<T> suggests(@NotNull List<String> staticChoices) { this.staticChoices = staticChoices; return this; }

        /** Provides a dynamic supplier of suggestions (evaluated on each tab-complete). */
        public Builder<T> suggests(@NotNull Supplier<List<String>> dynamicChoices) { this.dynamicChoices = dynamicChoices; return this; }

        /** Provides suggestions that depend on the command context (e.g., previously-typed arguments). */
        public Builder<T> suggestsContextual(@NotNull Function<CommandContext<CommandSourceStack>, List<String>> contextualChoices) {
            this.contextualChoices = contextualChoices;
            return this;
        }

        /** Provides async suggestions (e.g., from a database lookup). */
        public Builder<T> suggestsAsync(@NotNull Function<CommandContext<CommandSourceStack>, CompletableFuture<List<String>>> asyncChoices) {
            this.asyncChoices = asyncChoices;
            return this;
        }

        /** Hooks a custom Brigadier suggestion provider (used for advanced multi-token / flag completion). */
        public Builder<T> suggestsCustom(@NotNull SuggestionProvider<CommandSourceStack> customSuggestionProvider) {
            this.customSuggestionProvider = customSuggestionProvider;
            return this;
        }

        public VexelArg<T> build() {
            return new VexelArg<>(this);
        }
    }
}