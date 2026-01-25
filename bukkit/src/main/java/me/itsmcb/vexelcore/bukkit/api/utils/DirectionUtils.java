package me.itsmcb.vexelcore.bukkit.api.utils;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.entity.Player;

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
     * Compass direction enum with associated yaw values.
     */
    public enum Direction {
        NORTH(-180.0f),
        NORTHEAST(-135.0f),
        EAST(-90.0f),
        SOUTHEAST(-45.0f),
        SOUTH(0.0f),
        SOUTHWEST(45.0f),
        WEST(90.0f),
        NORTHWEST(135.0f);

        private final float yaw;

        Direction(float yaw) {
            this.yaw = yaw;
        }

        /**
         * Gets the yaw for this direction.
         *
         * @return yaw value
         */
        public float getYaw() {
            return yaw;
        }
    }
}
