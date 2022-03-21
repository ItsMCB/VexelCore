package me.itsmcb.vexelcore.bukkit.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BukkitUtils {

    public static String colorize(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void send(final @NotNull CommandSender sender, final @NotNull String... messages) {
        Arrays.stream(messages).forEach(message -> sender.sendMessage(colorize(message)));
    }
    public static void send(final @NotNull CommandSender sender, final @NotNull Component... messages) {
        Arrays.stream(messages).toList().forEach(sender::sendMessage);
    }

    public static Component componentize(String string) {
        return Component.text(BukkitUtils.colorize(string));
    }

    public static Component interactiveMessageRunCommand(String messageText, String messageHoverText, String commandToRun) {
        ClickEvent msgClickEvent = ClickEvent.runCommand(commandToRun);
        HoverEvent<Component> msgHoverEvent = HoverEvent.showText(BukkitUtils.componentize(messageHoverText));
        return BukkitUtils.componentize(messageText).clickEvent(msgClickEvent).hoverEvent(msgHoverEvent);
    }

    public static Component interactiveMessageOpenWebsite(String messageText, String messageHoverText, String websiteToOpen) {
        ClickEvent msgClickEvent = ClickEvent.openUrl(websiteToOpen);
        HoverEvent<Component> msgHoverEvent = HoverEvent.showText(BukkitUtils.componentize(messageHoverText));
        return BukkitUtils.componentize(messageText).clickEvent(msgClickEvent).hoverEvent(msgHoverEvent);
    }

}
