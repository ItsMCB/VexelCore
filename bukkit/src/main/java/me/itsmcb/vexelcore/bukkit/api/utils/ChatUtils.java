package me.itsmcb.vexelcore.bukkit.api.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChatUtils {

    /**
     * Creates a LegacyComponentSerializer that supports both
     * Minecraft-style color codes (e.g., "&a") and hexadecimal color codes
     * (e.g., "&#803ceb").
     *
     * @return A new LegacyComponentSerializer instance.
     */
    public static LegacyComponentSerializer getColorizer() {
        return LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build();
    }

    /**
     * Apply the & and hex color codes from the component text content to the component
     * @param component Component to transform
     * @return Component with in-text color codes applied
     */
    public static Component colorizeComponentText(@NotNull Component component) {
        return getColorizer().deserialize(
                getColorizer().serialize(component)
        );
    }

    /**
     * Recursively flattens a component and its children into a single string.
     * Only {@link TextComponent} are processed; other component types are ignored.
     *
     * @param component The component to flatten. Must not be null.
     * @return A string representation of the component's text content and the text content of its children,
     * or an empty string if the component is not a {@link TextComponent}.
     */
    public static String flattenComponent(@NotNull Component component) {
        if (component instanceof TextComponent textComponent) {
            StringBuilder builder = new StringBuilder(textComponent.content());
            List<Component> children = textComponent.children();
            for (Component child : children) {
                builder.append(flattenComponent(child));
            }
            return builder.toString();
        } else {
            return "";
        }
    }

    public static List<Audience> removePlayerAudience(@NotNull Set<Audience> viewers) {
        List<Audience> playerAudienceList = new ArrayList<>();
        viewers.stream()
                .filter(member -> member instanceof Player)
                .forEach(playerAudienceList::add);
        playerAudienceList.forEach(viewers::remove);
        return playerAudienceList;
    }

    public static String hexToColorCode(String hexValue) {
        StringBuilder formattedString = new StringBuilder("&x");
        // Remove the leading '#' if present
        if (hexValue.startsWith("#")) {
            hexValue = hexValue.substring(1);
        }
        // Iterate through each character in the hex value
        for (char c : hexValue.toCharArray()) {
            formattedString.append('&').append(c);
        }
        return formattedString.toString();
    }

}
