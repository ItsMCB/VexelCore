package me.itsmcb.vexelcore.bukkit.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CustomMsg {

    private String messageText;
    private String hoverText;
    private ClickEvent.Action clickEventAction;
    private String clickEventValue;

    CustomMsg(@NotNull MsgBuilder builder) {
        this.messageText = builder.getMessageText();
        this.hoverText = builder.getHoverText();
        this.clickEventAction = builder.getClickEventAction();
        this.clickEventValue = builder.getClickEventValue();
    }

    public void send(CommandSender sender) {
        sender.sendMessage(this::get);
    }

    public void sendAll() {
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(get()));
    }

    public @NotNull TextComponent get() {
        TextComponent.Builder component = Component.text().content(colorize(messageText));
        if (hoverText != null) {
            component.hoverEvent(HoverEvent.showText(componentize(hoverText)));
        }
        if (clickEventAction != null) {
            switch (clickEventAction) {
                case SUGGEST_COMMAND -> component.clickEvent(ClickEvent.suggestCommand(clickEventValue));
                case RUN_COMMAND -> component.clickEvent(ClickEvent.runCommand(clickEventValue));
                case OPEN_URL -> component.clickEvent(ClickEvent.openUrl(clickEventValue));
                case COPY_TO_CLIPBOARD -> component.clickEvent(ClickEvent.copyToClipboard(clickEventValue));
            }
        }
        return component.build();
    }

    // Internal

    private Component componentize(final @NotNull String string) {
        return Component.text(colorize(string));
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
