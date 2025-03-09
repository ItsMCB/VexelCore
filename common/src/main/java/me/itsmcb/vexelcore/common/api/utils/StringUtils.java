package me.itsmcb.vexelcore.common.api.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class StringUtils {

    // TODO remove
    public static String getCommaString(List<String> strings) {
        return String.join(", ", strings);
    }

    public static Pattern uuid = Pattern.compile("[0-9a-f]{8}(-[0-9a-f]{4}){4}[0-9a-f]{8}");
    public static Pattern uuidWithoutHyphens = Pattern.compile("^[0-9a-fA-F]{32}$");
    public static Pattern uuidFloodgate = Pattern.compile("00000000-0000-0000-0009-[0-9a-fA-F]{12}");

    public static boolean isUUID(@NotNull String string) {
        return uuid.matcher(string.toLowerCase()).matches() || uuidWithoutHyphens.matcher(string.toLowerCase()).matches();
    }

    /**
     * Derives a UUID from a string representation.
     * <p>
     * Supports UUID strings with or without hyphens.
     *
     * @param uuidAsString The string representation of a UUID.
     * @return The UUID object.
     * @throws IllegalArgumentException if the input string is not a valid UUID string.
     */
    public static UUID deriveUUID(@NotNull String uuidAsString) throws IllegalArgumentException {
        try {
            // Works if the string has hyphens
            return UUID.fromString(uuidAsString);
        } catch (IllegalArgumentException e) {
            // Attempts to add hyphens
            return UUID.fromString(uuidAsString.replaceAll("-","").replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5"));
        }
    }

    public static List<String> slitNth(String text, int n) {
        List<String> results = new ArrayList<>();
        int length = text.length();

        for (int i = 0; i < length; i += n) {
            results.add(text.substring(i, Math.min(length, i + n)));
        }

        return results;
    }

}
