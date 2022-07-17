package me.itsmcb.vexelcore.bukkit.api.utils;

import me.itsmcb.vexelcore.bukkit.api.experience.AudioResponse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Msg {

    public static String colorize(final @NotNull String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void send(final @NotNull CommandSender sender, final @NotNull String... messages) {
        Arrays.stream(messages).forEach(message -> sender.sendMessage(colorize(message)));
    }

    public static void sendResponsive(final @NotNull AudioResponse audioResponse, final @NotNull CommandSender sender, final @NotNull String... messages) {
        send(sender, messages);
        if (sender instanceof Player player) {
            switch (audioResponse) {
                case ERROR -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, (float) 1, 0);
                case INFO -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, (float) 1, 1);
                case SUCCESS -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, (float) 1, 2);
                default -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, (float) 1, 1);
            }
        }
    }

    public static @NotNull TextComponent sendOneLine(Component... components) {
        return Component.text().append(components).build();
    }

    public static @NotNull TextComponent sendOneLine(ArrayList<Component> components) {
        return Component.text().append(components).build();
    }

    public static @NotNull TextComponent sendOneLine(List<Component> components) {
        return Component.text().append(components).build();
    }

    public static void send(final @NotNull CommandSender sender, final @NotNull Component... messages) {
        send(sender, Arrays.stream(messages).toList());
    }

    public static void send(final @NotNull CommandSender sender, final @NotNull List<Component> messages) {
        messages.forEach(sender::sendMessage);
    }

    public static Component componentize(final @NotNull String string) {
        return Component.text(colorize(string));
    }

    public static Component runCommand(final @NotNull String messageText, final @NotNull String messageHoverText, final @NotNull String commandToRun) {
        ClickEvent msgClickEvent = ClickEvent.runCommand(commandToRun);
        return componentize(messageText).clickEvent(msgClickEvent).hoverEvent(getHoverComponent(messageHoverText));
    }

    public static Component suggestCommand(final @NotNull String messageText, final @NotNull String messageHoverText, final @NotNull String commandToSuggest) {
        ClickEvent msgClickEvent = ClickEvent.suggestCommand(commandToSuggest);
        return componentize(messageText).clickEvent(msgClickEvent).hoverEvent(getHoverComponent(messageHoverText));
    }

    public static Component openWebsite(final @NotNull String messageText, final @NotNull String messageHoverText, final @NotNull String websiteToOpen) {
        ClickEvent msgClickEvent = ClickEvent.openUrl(websiteToOpen);
        return componentize(messageText).clickEvent(msgClickEvent).hoverEvent(getHoverComponent(messageHoverText));
    }

    public static Component copyContent(final @NotNull String messageText, final @NotNull String messageHoverText, final @NotNull String contentToCopy) {
        ClickEvent msgClickEvent = ClickEvent.copyToClipboard(contentToCopy);
        return componentize(messageText).clickEvent(msgClickEvent).hoverEvent(getHoverComponent(messageHoverText));
    }

    public static HoverEvent<Component> getHoverComponent(final @NotNull String hoverText) {
        return HoverEvent.showText(componentize(hoverText));
    }


}
