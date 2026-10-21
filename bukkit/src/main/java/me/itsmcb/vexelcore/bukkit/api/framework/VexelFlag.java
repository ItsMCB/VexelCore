package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a command flag specification (e.g., {@code --difficulty <value>} or {@code --no-structures}).
 * Supports boolean flags, single-valued flags, multi-valued flags with contextual positional suggestions,
 * and repeatable flags.
 */
public class VexelFlag {

    @FunctionalInterface
    public interface FlagValueSuggester {
        /**
         * Computes suggestions for a flag's value based on its position and prior tokens.
         *
         * @param ctx            Command context.
         * @param valueIndex     Index of the value currently being typed (0-based).
         * @param previousValues Prior values already supplied for this flag occurrence.
         * @return List of suggestions.
         */
        @NotNull List<String> suggest(
                @NotNull CommandContext<CommandSourceStack> ctx,
                int valueIndex,
                @NotNull List<String> previousValues
        );
    }

    private final String name;
    private final List<String> aliases;
    private final String description;
    private final boolean valued;
    private final int valueCount;
    private final boolean repeatable;
    @Nullable private final FlagValueSuggester valueSuggester;

    private VexelFlag(
            @NotNull String name,
            @NotNull List<String> aliases,
            @NotNull String description,
            boolean valued,
            int valueCount,
            boolean repeatable,
            @Nullable FlagValueSuggester valueSuggester
    ) {
        this.name = name.toLowerCase(Locale.ROOT);
        this.aliases = aliases.stream().map(a -> a.toLowerCase(Locale.ROOT)).toList();
        this.description = description;
        this.valued = valued;
        this.valueCount = valueCount;
        this.repeatable = repeatable;
        this.valueSuggester = valueSuggester;
    }

    // ── Factory Methods ──────────────────────────────────────────────────

    /**
     * Creates a boolean flag (expects no following value; presence indicates true).
     */
    public static VexelFlag booleanFlag(@NotNull String name, @NotNull String description) {
        return new VexelFlag(name, List.of(), description, false, 0, false, null);
    }

    /**
     * Creates a single-valued flag without specific value suggestions.
     */
    public static VexelFlag valued(@NotNull String name, @NotNull String description) {
        return new VexelFlag(name, List.of(), description, true, 1, false, null);
    }

    /**
     * Creates a single-valued flag with a static list of suggestions.
     */
    public static VexelFlag valued(@NotNull String name, @NotNull String description, @NotNull List<String> staticSuggestions) {
        return new VexelFlag(name, List.of(), description, true, 1, false, (ctx, idx, prev) -> staticSuggestions);
    }

    /**
     * Creates a single-valued flag with dynamic suggestions evaluated on tab-complete.
     */
    public static VexelFlag valued(@NotNull String name, @NotNull String description, @NotNull Supplier<List<String>> dynamicSuggestions) {
        return new VexelFlag(name, List.of(), description, true, 1, false, (ctx, idx, prev) -> dynamicSuggestions.get());
    }

    /**
     * Creates a single-valued flag with context-dependent suggestions.
     */
    public static VexelFlag valued(@NotNull String name, @NotNull String description, @NotNull Function<CommandContext<CommandSourceStack>, List<String>> suggester) {
        return new VexelFlag(name, List.of(), description, true, 1, false, (ctx, idx, prev) -> suggester.apply(ctx));
    }

    /**
     * Creates a multi-valued flag expecting an exact number of arguments (e.g. {@code --spawn <x> <y> <z>} expects 3).
     */
    public static VexelFlag multiValued(@NotNull String name, @NotNull String description, int valueCount) {
        return new VexelFlag(name, List.of(), description, true, Math.max(1, valueCount), false, null);
    }

    /**
     * Creates a multi-valued flag with a simple context-dependent suggester applied to the first value.
     */
    public static VexelFlag multiValued(@NotNull String name, @NotNull String description, int valueCount, @NotNull Function<CommandContext<CommandSourceStack>, List<String>> suggester) {
        return new VexelFlag(name, List.of(), description, true, Math.max(1, valueCount), false, (ctx, idx, prev) -> suggester.apply(ctx));
    }

    /**
     * Creates a multi-valued flag with a position-aware and previous-value-aware suggester.
     */
    public static VexelFlag multiValued(@NotNull String name, @NotNull String description, int valueCount, @NotNull FlagValueSuggester suggester) {
        return new VexelFlag(name, List.of(), description, true, Math.max(1, valueCount), false, suggester);
    }

    // ── Fluent Modifiers ─────────────────────────────────────────────────

    /**
     * Adds alternative alias names for this flag (without leading dashes).
     */
    public VexelFlag alias(@NotNull String... aliases) {
        List<String> combined = new ArrayList<>(this.aliases);
        for (String a : aliases) {
            String clean = a.replaceFirst("^-+", "").toLowerCase(Locale.ROOT);
            if (!clean.isBlank() && !combined.contains(clean)) {
                combined.add(clean);
            }
        }
        return new VexelFlag(name, combined, description, valued, valueCount, repeatable, valueSuggester);
    }

    /**
     * Marks this flag as repeatable, allowing it to be specified multiple times in the same command
     * (e.g., setting multiple game rules: {@code --gamerule A 1 --gamerule B 2}).
     */
    public VexelFlag repeatable() {
        return new VexelFlag(name, aliases, description, valued, valueCount, true, valueSuggester);
    }

    /**
     * Explicitly sets whether this flag is repeatable.
     */
    public VexelFlag repeatable(boolean repeatable) {
        return new VexelFlag(name, aliases, description, valued, valueCount, repeatable, valueSuggester);
    }

    // ── Getters & Matching ───────────────────────────────────────────────

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<String> getAliases() {
        return aliases;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public boolean isValued() {
        return valued;
    }

    public int getValueCount() {
        return valueCount;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public @NotNull String getPrefixedName() {
        return "--" + name;
    }

    public @NotNull List<String> getPrefixedAliases() {
        return aliases.stream()
                .map(a -> (a.length() == 1 ? "-" : "--") + a)
                .toList();
    }

    /**
     * Checks if a user-supplied token matches this flag's canonical name or any alias.
     */
    public boolean matches(@NotNull String token) {
        String clean = token.replaceFirst("^-+", "").toLowerCase(Locale.ROOT);
        return clean.equals(name) || aliases.contains(clean);
    }

    public @NotNull List<String> getValueSuggestions(
            @NotNull CommandContext<CommandSourceStack> ctx,
            int valueIndex,
            @NotNull List<String> previousValues
    ) {
        if (valueSuggester == null) return Collections.emptyList();
        try {
            List<String> res = valueSuggester.suggest(ctx, valueIndex, previousValues);
            return res != null ? res : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}