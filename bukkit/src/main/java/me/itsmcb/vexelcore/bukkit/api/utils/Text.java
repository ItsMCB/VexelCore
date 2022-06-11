package me.itsmcb.vexelcore.bukkit.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Text {

    public static String colorize(final @NotNull String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void send(final @NotNull CommandSender sender, final @NotNull String... messages) {
        Arrays.stream(messages).forEach(message -> sender.sendMessage(colorize(message)));
    }
    public static void send(final @NotNull CommandSender sender, final @NotNull Component... messages) {
        Arrays.stream(messages).toList().forEach(sender::sendMessage);
    }

    public static Component componentize(final @NotNull String string) {
        return Component.text(colorize(string));
    }

    public static Component runCommand(final @NotNull String messageText, final @NotNull String messageHoverText, final @NotNull String commandToRun) {
        ClickEvent msgClickEvent = ClickEvent.runCommand(commandToRun);
        HoverEvent<Component> msgHoverEvent = HoverEvent.showText(componentize(messageHoverText));
        return componentize(messageText).clickEvent(msgClickEvent).hoverEvent(msgHoverEvent);
    }

    public static Component openWebsite(final @NotNull String messageText, final @NotNull String messageHoverText, final @NotNull String websiteToOpen) {
        ClickEvent msgClickEvent = ClickEvent.openUrl(websiteToOpen);
        HoverEvent<Component> msgHoverEvent = HoverEvent.showText(componentize(messageHoverText));
        return componentize(messageText).clickEvent(msgClickEvent).hoverEvent(msgHoverEvent);
    }

    public static Component copyContent(final @NotNull String messageText, final @NotNull String messageHoverText, final @NotNull String contentToCopy) {
        ClickEvent msgClickEvent = ClickEvent.copyToClipboard(contentToCopy);
        HoverEvent<Component> msgHoverEvent = HoverEvent.showText(componentize(messageHoverText));
        return componentize(messageText).clickEvent(msgClickEvent).hoverEvent(msgHoverEvent);
    }
}
