package me.itsmcb.vexelcore.bukkit.api.cache;

import me.itsmcb.vexelcore.common.api.utils.StringUtils;
import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GeyserUtils {

    private static final String FLOODGATE_MISSING_DOT_FOUND = "It appears Bedrock player data is being requested as indicated by a dot. However, Floodgate is missing; this will cause plugin issues!";
    private static final String FLOODGATE_MISSING_UUID_FRAGMENT_FOUND = "It appears Bedrock player data is being requested as indicated by the UUID fragment with zeroes. However, Floodgate is missing; this will cause plugin issues!";
    private static final String FLOODGATE_MISSING_GEYSER_FOUND = "It appears Geyser is installed without floodgate. This will cause plugin issues!";

    /**
     * Formats a UUID string with dashes.
     *
     * @param uuid UUID string without dashes
     * @return UUID string with dashes
     */
    public static String formatUUID(String uuid) {
        if (uuid.length() == 32) {
            return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" +
                    uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
        }
        return uuid;
    }

    public static String bedrockUUIDPart = "00000000-0000-0000";

    /**
     * Determines if Floodgate is installed
     *
     * @return True if Floodgate is installed
     */
    public static boolean floodgateIsInstalled() {
        if (!Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            if (Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot")) {
                Bukkit.getPluginManager().getPlugin("VexelCore").getLogger().severe(FLOODGATE_MISSING_GEYSER_FOUND);
            }
            return false;
        }
        return true;
    }

    /**
     * Determines if the username has a Geyser prefix indicating that it's a Bedrock Edition player
     *
     * @return True if the username has the Geyser prefix
     */
    public static boolean isBedrock(@NotNull String username) {
        if (!floodgateIsInstalled()) {
            // Geyser and Floodgate aren't installed. However, since the dot is the default prefix, provide a warning.
            if (username.startsWith(".")) {
                Bukkit.getPluginManager().getPlugin("VexelCore").getLogger().severe(FLOODGATE_MISSING_DOT_FOUND);
            }
            return false;
        }
        return username.startsWith(FloodgateApi.getInstance().getPlayerPrefix());
    }

    /**
     * Determines if the UUID is formatted for a Geyser (Bedrock Edition) player (XUID).
     *
     * @return True if it's a UUID derived from an Xbox User ID (XUID)
     */
    public static boolean isBedrock(@NotNull UUID UUID) {
        if (!floodgateIsInstalled()) {
            if (UUID.toString().contains(bedrockUUIDPart)) {
                Bukkit.getPluginManager().getPlugin("VexelCore").getLogger().severe(FLOODGATE_MISSING_UUID_FRAGMENT_FOUND);
            }
            return false;
        }
        return FloodgateApi.getInstance().isFloodgateId(UUID);
    }

    /**
     * Converts a Geyser Bedrock player UUID to an Xbox User ID (XUID).
     * <p>
     * Geyser embeds the XUID in the UUID for Bedrock players using a specific pattern:
     * - The first component is always 00000000
     * - The second component is always 0000
     * - The third component is always 0000
     * - The fourth component is always 0009
     * - The fifth component contains the XUID encoded as a hex string
     *
     * @param uuid The UUID string of the Geyser Bedrock player (format: 00000000-0000-0000-0009-XXXXXXXXXXXX)
     * @return The XUID as a long value, or -1 if the UUID is not in the expected Geyser format
     */
    public static long getXUIDFromGeyserUUID(@NotNull UUID uuid) {
        // Validate the basic structure of a Geyser Bedrock UUID
        if (!uuid.toString().matches(StringUtils.uuidFloodgate.pattern())) {
            return -1; // Not a valid Geyser Bedrock UUID
        }
        // Extract the last component which contains the XUID in hex
        String xuidHexString = uuid.toString().substring(24); // Get everything after the last dash
        try {
            return Long.parseLong(xuidHexString, 16);
        } catch (NumberFormatException e) {
            return -1; // Failed to parse the hex string
        }
    }

    /**
     * Constructs a Geyser Bedrock UUID from an XUID.
     * <p>
     * Given an XUID (Xbox User ID), construct a Geyser Bedrock UUID by embedding the XUID into the specific format.
     *
     * @param xuid The Xbox User ID (XUID) as a long.
     * @return Valid Geyser Bedrock UUID or null if the XUID is invalid
     */
    public static UUID getGeyserUUIDFromXUID(long xuid) {
        if (xuid < 0) {
            return null; // XUID should not be negative
        }

        // Ensure the hex string is 12 characters long by padding with leading zeros if necessary
        String xuidHexString = String.format("%012x", xuid); // %012x formats as hex with leading zeros to 12 digits

        // Construct the Geyser Bedrock UUID string by prefixing the standard parts and the XUID hex
        String geyserUUIDString = "00000000-0000-0000-0009-" + xuidHexString;

        try {
            return UUID.fromString(geyserUUIDString);
        } catch (IllegalArgumentException e) {
            // This might happen if the constructed string is not a valid UUID format
            return null;
        }
    }
}
