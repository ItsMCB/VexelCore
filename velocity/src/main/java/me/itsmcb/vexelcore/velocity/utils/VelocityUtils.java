package me.itsmcb.vexelcore.velocity.utils;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityUtils {

    public static TextComponent parseLegacy(String... input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(String.join(" ", input));
    }

    public static void sendMsg(CommandSource source, String... input) {
        for (String text : input) {
            source.sendMessage(parseLegacy(text));
        }
    }
}
