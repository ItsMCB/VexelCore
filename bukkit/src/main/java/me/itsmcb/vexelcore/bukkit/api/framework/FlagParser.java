package me.itsmcb.vexelcore.bukkit.api.framework;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Modern flag and argument parser driven strictly by a {@link VexelFlags} specification.
 * Distinguishes between flags and positional arguments, respecting flag value counts
 * and resolving aliases to canonical flag names automatically.
 */
public class FlagParser {

    private final List<String> positionalArgs = new ArrayList<>();
    private final Map<String, List<String>> flags = new HashMap<>();

    public FlagParser(@Nullable String input, @NotNull VexelFlags spec) {
        if (input == null || input.isBlank()) return;

        List<String> parts = tokenize(input);

        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (isFlag(part)) {
                String key = part.replaceFirst("^-+", "").toLowerCase(Locale.ROOT);
                VexelFlag flag = spec.findFlag(key);

                if (flag != null) {
                    List<String> values = new ArrayList<>();
                    while (i + 1 < parts.size() && values.size() < flag.getValueCount() && !isFlag(parts.get(i + 1))) {
                        values.add(parts.get(++i));
                    }
                    flags.computeIfAbsent(flag.getName(), k -> new ArrayList<>()).addAll(values);
                } else {
                    // Unknown flag: treat as boolean flag under its stripped name
                    flags.putIfAbsent(key, Collections.emptyList());
                }
            } else {
                positionalArgs.add(part);
            }
        }
    }

    /**
     * Creates an empty parser with no flags or positional arguments.
     */
    public static @NotNull FlagParser empty() {
        return new FlagParser(null, VexelFlags.empty());
    }

    private static @NotNull List<String> tokenize(@NotNull String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (current.length() > 0) {
                    parts.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static boolean isFlag(@Nullable String part) {
        if (part == null || part.length() <= 1 || !part.startsWith("-")) return false;
        try {
            Double.parseDouble(part);
            return false;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    public @NotNull List<String> getPositionalArgs() {
        return Collections.unmodifiableList(positionalArgs);
    }

    public boolean hasFlag(@NotNull String key) {
        return flags.containsKey(key.toLowerCase(Locale.ROOT));
    }

    public @NotNull Optional<String> getValue(@NotNull String key) {
        List<String> values = flags.get(key.toLowerCase(Locale.ROOT));
        if (values != null && !values.isEmpty()) {
            return Optional.of(values.get(0));
        }
        return Optional.empty();
    }

    public @NotNull List<String> getValues(@NotNull String key) {
        return flags.getOrDefault(key.toLowerCase(Locale.ROOT), Collections.emptyList());
    }
}