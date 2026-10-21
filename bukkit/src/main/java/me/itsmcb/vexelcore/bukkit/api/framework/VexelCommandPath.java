package me.itsmcb.vexelcore.bukkit.api.framework;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable tracker for a command's full path from root to the current node.
 * Used for accurate help generation and error messages across nested command trees.
 *
 * <p>Example: for {@code /lp v create}, the {@code create} node's path is {@code ["lp", "v", "create"]}.</p>
 */
public final class VexelCommandPath {

    private final List<String> segments;

    /** Creates an empty path (used by root commands). */
    public VexelCommandPath() {
        this.segments = List.of();
    }

    private VexelCommandPath(@NotNull List<String> parentSegments, @NotNull String childName) {
        var newList = new ArrayList<String>(parentSegments.size() + 1);
        newList.addAll(parentSegments);
        newList.add(childName);
        this.segments = List.copyOf(newList);
    }

    /**
     * Creates a child path by appending the given name.
     *
     * @param name The name of the next segment.
     * @return A new {@link VexelCommandPath} with the segment appended.
     */
    public @NotNull VexelCommandPath child(@NotNull String name) {
        return new VexelCommandPath(this.segments, name);
    }

    /**
     * Returns the path as a space-separated string (e.g. {@code "lp v create"}).
     *
     * @return The path string without a leading slash.
     */
    public @NotNull String asString() {
        return String.join(" ", segments);
    }

    /**
     * Returns the path as a command string (e.g. {@code "/lp v create"}).
     *
     * @return The path string with a leading slash.
     */
    public @NotNull String asCommand() {
        return "/" + asString();
    }

    /**
     * Returns the individual path segments.
     *
     * @return An unmodifiable list of segment strings.
     */
    public @NotNull List<String> getSegments() {
        return segments;
    }

    /**
     * Checks if this path is empty (i.e., represents the root).
     *
     * @return {@code true} if the path has no segments.
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    @Override
    public @NotNull String toString() {
        return asCommand();
    }
}