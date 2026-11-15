package me.itsmcb.vexelcore.bukkit.api.text;

import me.itsmcb.vexelcore.common.api.text.CommonMsgBuilder;
import me.itsmcb.vexelcore.common.api.text.CustomMsg;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@SerializableAs("MsgBuilder")
public class BukkitMsgBuilder extends CommonMsgBuilder implements ConfigurationSerializable {

    public BukkitMsgBuilder(String messageText) {
        super(messageText);
    }

    public BukkitMsgBuilder messageText(@NotNull String messageText) {
        return (BukkitMsgBuilder) super.text(messageText);
    }

    public BukkitMsgBuilder hover(@NotNull String hoverText) {
        return (BukkitMsgBuilder) super.hover(hoverText);
    }

    public BukkitMsgBuilder clickEvent(@NotNull ClickEvent.Action clickEventAction, @NotNull String clickEventValue) {
        return (BukkitMsgBuilder) super.clickEvent(clickEventAction, clickEventValue);
    }

    public CustomMsg build() {
        CustomMsg msg = new CustomMsg(this);
        return msg;
    }

    public void send(CommandSender sender) {
        sender.sendMessage(new CustomMsg(this).get());
    }

    public void sendAll() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            send(onlinePlayer);
        }
    }

    public void sendAllExcept(@NotNull List<Player> excluded) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!excluded.contains(onlinePlayer)) {
                send(onlinePlayer);
            }
        }
    }

    public void sendAllExcept(@NotNull Player excluded) {
        sendAllExcept(List.of(excluded));
    }

    public void sendOnly(@NotNull List<Player> included) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (included.contains(onlinePlayer)) {
                send(onlinePlayer);
            }
        }
    }

    public void sendOnly(@NotNull Player included) {
        sendOnly(List.of(included));
    }

    public void sendActionBar(int repeatSeconds, JavaPlugin instance, Predicate<Player> shouldContinue, Player player) {
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count == repeatSeconds || !shouldContinue.test(player)) {
                    this.cancel();
                }
                player.sendActionBar(get());
                count++;
            }
        }.runTaskTimerAsynchronously(instance, 0L, 20L);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        if (super.getMessageText() != null) {
            map.put("message-text", getMessageText());
        }
        if (getHoverText() != null) {
            map.put("hover-text", getHoverText());
        }
        if (getClickEventAction() != null) {
            map.put("click-event-action", serializeClickEventAction(getClickEventAction()));
        }
        if (getClickEventValue() != null) {
            map.put("click-event-value", getClickEventValue());
        }
        return map;
    }

    public static BukkitMsgBuilder deserialize(Map<String, Object> map) {
        String messageText = map.get("message-text") instanceof String text ? text : "";
        BukkitMsgBuilder msgBuilder = new BukkitMsgBuilder(messageText);
        if (map.get("hover-text") instanceof String hoverText) {
            msgBuilder.hover(hoverText);
        }
        if (map.get("click-event-action") instanceof String action && map.get("click-event-value") instanceof String value) {
            if (!action.isBlank() && !value.isBlank()) {
                ClickEvent.Action parsedAction = parseClickEventAction(action);
                if (parsedAction != null) {
                    msgBuilder.clickEvent(parsedAction, value);
                }
            }
        }
        return msgBuilder;
    }

    private static ClickEvent.Action parseClickEventAction(String action) {
        return switch (action.toUpperCase(Locale.ROOT)) {
            case "OPEN_URL" -> ClickEvent.Action.OPEN_URL;
            case "OPEN_FILE" -> ClickEvent.Action.OPEN_FILE;
            case "RUN_COMMAND" -> ClickEvent.Action.RUN_COMMAND;
            case "SUGGEST_COMMAND" -> ClickEvent.Action.SUGGEST_COMMAND;
            case "CHANGE_PAGE" -> ClickEvent.Action.CHANGE_PAGE;
            case "COPY_TO_CLIPBOARD" -> ClickEvent.Action.COPY_TO_CLIPBOARD;
            default -> null;
        };
    }

    private static String serializeClickEventAction(ClickEvent.Action action) {
        if (Objects.equals(action, ClickEvent.Action.OPEN_URL)) {
            return "OPEN_URL";
        }
        if (Objects.equals(action, ClickEvent.Action.OPEN_FILE)) {
            return "OPEN_FILE";
        }
        if (Objects.equals(action, ClickEvent.Action.RUN_COMMAND)) {
            return "RUN_COMMAND";
        }
        if (Objects.equals(action, ClickEvent.Action.SUGGEST_COMMAND)) {
            return "SUGGEST_COMMAND";
        }
        if (Objects.equals(action, ClickEvent.Action.CHANGE_PAGE)) {
            return "CHANGE_PAGE";
        }
        if (Objects.equals(action, ClickEvent.Action.COPY_TO_CLIPBOARD)) {
            return "COPY_TO_CLIPBOARD";
        }
        return action.toString();
    }
}