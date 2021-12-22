package me.itsmcb.vexelcore.bukkit.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BukkitUtils {

    public static String colorize(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void send(final @NotNull CommandSender sender, String... messages) {
        Arrays.stream(messages).forEach(message -> sender.sendMessage(colorize(message)));
    }

    public static Component componentize(String string) {
        return Component.text(BukkitUtils.colorize(string));
    }

}
