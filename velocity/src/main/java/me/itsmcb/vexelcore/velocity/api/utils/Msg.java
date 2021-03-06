package me.itsmcb.vexelcore.velocity.api.utils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Arrays;

public class Msg {

    public static TextComponent colorize(String... input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(String.join(" ", input));
    }

    public static void send(CommandSource source, String... input) {
        Arrays.stream(input).forEach(msg -> source.sendMessage(colorize(msg)));
    }

    public static void send(Player player, String... input) {
        Arrays.stream(input).forEach(msg -> player.sendMessage(colorize(msg)));
    }
}
