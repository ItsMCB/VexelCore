package me.itsmcb.vexelcore.bukkit.api.framework;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

    /**
     * Escapes single quotes for safe embedding inside MiniMessage tag attributes.
     */
    public static @NotNull String escapeSingleQuotes(@NotNull String input) {
        return input.replace("'", "\\'");
    }

    /**
     * Sends a tree-formatted status line whose value copies to clipboard when clicked.
     */
    public static void sendCopyable(@NotNull CommandSender sender, @NotNull String label, @NotNull String value) {
        send(sender,
                "<dark_gray>╠═ <gray><label>: <click:copy_to_clipboard:'" + escapeSingleQuotes(value) + "'><hover:show_text:'<gray>Click to copy</gray>'><dark_aqua><val></dark_aqua></hover></click></gray></dark_gray>",
                Placeholder.unparsed("label", label),
                Placeholder.unparsed("val", value));
    }

    /**
     * Sends a tree-formatted status line whose value is hidden until hovered and copies to clipboard when clicked.
     */
    public static void sendHiddenCopyable(@NotNull CommandSender sender, @NotNull String label, @NotNull String value) {
        send(sender,
                "<dark_gray>╠═ <gray><label>: <click:copy_to_clipboard:'" + escapeSingleQuotes(value) + "'><hover:show_text:'<gray>Click to copy: <dark_aqua><val></dark_aqua></gray>'><dark_aqua>Hidden (hover to view)</dark_aqua></hover></click></gray></dark_gray>",
                Placeholder.unparsed("label", label),
                Placeholder.unparsed("val", value));
    }

    /**
     * Sends a tree-formatted status line whose value opens a URL when clicked.
     */
    public static void sendUrl(@NotNull CommandSender sender, @NotNull String label, @NotNull String url, @NotNull String hover) {
        send(sender,
                "<dark_gray>╠═ <gray><label>: <click:open_url:'" + escapeSingleQuotes(url) + "'><hover:show_text:'<gray><hover></gray>'><dark_aqua><url></dark_aqua></hover></click></gray></dark_gray>",
                Placeholder.unparsed("label", label),
                Placeholder.unparsed("hover", hover),
                Placeholder.unparsed("url", url));
    }
}