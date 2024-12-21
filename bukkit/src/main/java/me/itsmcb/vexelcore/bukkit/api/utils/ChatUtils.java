package me.itsmcb.vexelcore.bukkit.api.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChatUtils {

    public static LegacyComponentSerializer getColorizer() {
        return LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build();
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
