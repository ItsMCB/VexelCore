package me.itsmcb.vexelcore.bukkit.api.utils;

import me.itsmcb.vexelcore.bukkit.api.framework.TextUtils;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for handling directional operations.
 */
public final class DirectionUtils {

    /**
     * Cached lookup map for fast, case-insensitive parsing.
     */
    private static final Map<String, Direction> DIRECTIONS_BY_NAME =
            Arrays.stream(Direction.values())
                    .collect(Collectors.toMap(
                            d -> d.name().toLowerCase(Locale.ROOT),
                            d -> d
                    ));

    /**
     * Sorted list of direction names for tab completion.
     */
    private static final List<String> DIRECTION_COMPLETIONS =
            Arrays.stream(Direction.values())
                    .map(Direction::name)
                    .sorted()
                    .toList();

    /**
     * Standard list of compass, relative, and numeric yaw suggestions for commands.
     */
    private static final List<String> RELATIVE_AND_COMPASS_COMPLETIONS = List.of(
            "north", "south", "east", "west",
            "left", "right", "back", "forward",
            "0", "90", "180", "270"
    );

    /**
     * Parses a direction string into a Direction enum.
     *
     * @param input Direction string (case-insensitive)
     * @return Optional Direction
     */
    public static Optional<Direction> parseDirection(String input) {
        if (input == null) return Optional.empty();
        return Optional.ofNullable(
                DIRECTIONS_BY_NAME.get(input.toLowerCase(Locale.ROOT))
        );
    }

    /**
     * Parses a direction string into a yaw value.
     *
     * @param input Direction string
     * @return Optional yaw value
     */
    public static Optional<Float> parseYaw(String input) {
        return parseDirection(input).map(Direction::getYaw);
    }

    /**
     * Resolves yaw input from:
     * - Relative directions (forward/f, back/b, left/l, right/r)
     * - Numeric yaw values
     * - Compass directions (north, southeast, etc.)
     *
     * @param input   Raw direction or yaw input
     * @param baseYaw Base yaw for relative adjustments
     * @return Optional resolved yaw
     */
    public static Optional<Float> resolveYaw(@Nullable String input, float baseYaw) {
        if (input == null || input.isBlank()) {
            return Optional.of(baseYaw);
        }
        String lower = input.trim().toLowerCase(Locale.ROOT);

        // Relative directions
        switch (lower) {
            case "forward", "f" -> {
                return Optional.of(baseYaw);
            }
            case "back", "b" -> {
                return Optional.of(baseYaw + 180.0f);
            }
            case "left", "l" -> {
                return Optional.of(baseYaw - 90.0f);
            }
            case "right", "r" -> {
                return Optional.of(baseYaw + 90.0f);
            }
        }

        // Numeric yaw
        try {
            float parsed = Float.parseFloat(lower);
            if (Float.isFinite(parsed)) {
                return Optional.of(parsed);
            }
        } catch (NumberFormatException ignored) {
            // Fall through to compass direction
        }

        // Compass direction
        return parseYaw(lower);
    }

    /**
     * Resolves yaw input (compass, relative, or numeric) and sends an error message if invalid.
     *
     * @param input   Raw direction or yaw input
     * @param player  Player to notify if invalid
     * @param baseYaw Base yaw for relative adjustments
     * @return Optional resolved yaw
     */
    public static Optional<Float> resolveYawWithError(@Nullable String input, @NotNull Player player, float baseYaw) {
        Optional<Float> resolved = resolveYaw(input, baseYaw);
        if (resolved.isEmpty()) {
            TextUtils.send(player,
                    "<red>Invalid direction! Use compass directions, relative directions <gray>(left/right/back)</gray>, or a numeric yaw.</red>");
        }
        return resolved;
    }

    /**
     * Parses a direction string and sends an error message if invalid.
     *
     * @param input Direction string
     * @param player Player to notify
     * @return Optional yaw value
     */
    public static Optional<Float> parseYawWithError(String input, Player player) {
        Optional<Float> yaw = parseYaw(input);
        if (yaw.isEmpty()) {
            sendInvalidDirectionMessage(player);
        }
        return yaw;
    }

    /**
     * Gets the closest compass direction to the given yaw.
     *
     * @param yaw Raw yaw value
     * @return Closest Direction
     */
    public static Direction getClosestDirection(float yaw) {
        float normalized = normalizeYaw(yaw);

        return Arrays.stream(Direction.values())
                .min(Comparator.comparingDouble(
                        d -> Math.abs(normalized - d.getYaw())
                ))
                .orElse(Direction.SOUTH);
    }

    /**
     * Converts a yaw value into its closest compass arrow symbol.
     *
     * @param yaw Raw yaw value
     * @return Compass arrow string
     */
    public static @NotNull String getDirectionArrow(float yaw) {
        return getClosestDirection(yaw).getArrow();
    }

    /**
     * Gets a human-readable direction name from yaw.
     * Returns CUSTOM if no exact match is found.
     *
     * @param yaw Raw yaw value
     * @return Direction name
     */
    public static String getDirectionLabel(float yaw) {
        float normalized = normalizeYaw(yaw);

        for (Direction direction : Direction.values()) {
            if (Math.abs(normalized - direction.getYaw()) <= 1.0f) {
                return direction.name();
            }
        }

        return "CUSTOM (" + String.format(Locale.ROOT, "%.1f", normalized) + "°)";
    }

    /**
     * Normalizes yaw to Minecraft's standard range (-180 to 180).
     *
     * @param yaw Raw yaw
     * @return Normalized yaw
     */
    public static float normalizeYaw(float yaw) {
        while (yaw > 180.0f) yaw -= 360.0f;
        while (yaw < -180.0f) yaw += 360.0f;
        return yaw;
    }

    /**
     * Converts yaw (-180 to 180) to NPC yaw (0 to 360).
     *
     * @param yaw Minecraft yaw
     * @return NPC yaw
     */
    public static float toNpcYaw(float yaw) {
        float normalized = normalizeYaw(yaw);
        return normalized < 0 ? normalized + 360.0f : normalized;
    }

    /**
     * Gets valid direction names for tab completion.
     *
     * @return Immutable list of direction names
     */
    public static List<String> getDirectionCompletions() {
        return DIRECTION_COMPLETIONS;
    }

    /**
     * Gets standard compass, relative, and numeric yaw suggestions for tab completion.
     *
     * @return Immutable list of direction completions
     */
    public static @NotNull List<String> getRelativeAndCompassCompletions() {
        return RELATIVE_AND_COMPASS_COMPLETIONS;
    }

    /**
     * Sends an error message for invalid direction input.
     *
     * @param player Player to send message to
     */
    public static void sendInvalidDirectionMessage(Player player) {
        String validDirections = String.join(", ", DIRECTION_COMPLETIONS);
        new BukkitMsgBuilder("&cInvalid direction! Valid directions: " + validDirections)
                .send(player);
    }

    /**
     * Compass direction enum with associated yaw values and arrows.
     */
    public enum Direction {
        NORTH(-180.0f, "⬆"),
        NORTHEAST(-135.0f, "⬈"),
        EAST(-90.0f, "➡"),
        SOUTHEAST(-45.0f, "⬊"),
        SOUTH(0.0f, "⬇"),
        SOUTHWEST(45.0f, "⬋"),
        WEST(90.0f, "⬅"),
        NORTHWEST(135.0f, "⬉");

        private final float yaw;
        private final String arrow;

        Direction(float yaw, String arrow) {
            this.yaw = yaw;
            this.arrow = arrow;
        }

        /**
         * Gets the yaw for this direction.
         *
         * @return yaw value
         */
        public float getYaw() {
            return yaw;
        }

        /**
         * Gets the arrow symbol for this direction.
         *
         * @return arrow string
         */
        public @NotNull String getArrow() {
            return arrow;
        }
    }
}