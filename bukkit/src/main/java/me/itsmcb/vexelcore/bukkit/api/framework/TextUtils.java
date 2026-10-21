package me.itsmcb.vexelcore.bukkit.api.framework;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for MiniMessage text parsing and sending.
 *
 * <p>Provides two {@link MiniMessage} instances:</p>
 * <ul>
 *   <li>{@link #mm(String, TagResolver...)} — trusted, parses all tags (for plugin-internal strings).</li>
 *   <li>{@link #mmSafe(String)} — safe, parses only formatting tags (for user-supplied input).</li>
 * </ul>
 */
public final class TextUtils {

    private static final MiniMessage TRUSTED = MiniMessage.miniMessage();
    private static final MiniMessage SAFE = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(), StandardTags.decorations(), StandardTags.reset(),
                    StandardTags.newline(), StandardTags.gradient(), StandardTags.rainbow(),
                    StandardTags.transition()
            ))
            .build();

    private TextUtils() {}

    /**
     * Parses a MiniMessage string with all tags enabled (trusted input).
     *
     * @param input     The MiniMessage string.
     * @param resolvers Optional tag resolvers.
     * @return The parsed {@link Component}.
     */
    public static @NotNull Component mm(@NotNull String input, @NotNull TagResolver... resolvers) {
        return TRUSTED.deserialize(input, resolvers);
    }

    /**
     * Sends a MiniMessage string to a sender (trusted input).
     *
     * @param sender    The command sender.
     * @param input     The MiniMessage string.
     * @param resolvers Optional tag resolvers.
     */
    public static void send(@NotNull CommandSender sender, @NotNull String input, @NotNull TagResolver... resolvers) {
        sender.sendMessage(mm(input, resolvers));
    }

    /**
     * Parses a MiniMessage string with only formatting tags (safe for user input).
     *
     * @param input The MiniMessage string.
     * @return The parsed {@link Component}.
     */
    public static @NotNull Component mmSafe(@NotNull String input) {
        return SAFE.deserialize(input);
    }

    /**
     * Sends a MiniMessage string to a sender (safe for user input).
     *
     * @param sender The command sender.
     * @param input  The MiniMessage string.
     */
    public static void sendSafe(@NotNull CommandSender sender, @NotNull String input) {
        sender.sendMessage(mmSafe(input));
    }
}