package me.itsmcb.vexelcore.bukkit.api.text;

import me.itsmcb.vexelcore.common.api.text.CommonMsgBuilder;
import me.itsmcb.vexelcore.common.api.text.CustomMsg;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@SerializableAs("MsgBuilder")
public class BukkitMsgBuilder extends CommonMsgBuilder implements ConfigurationSerializable {

    public BukkitMsgBuilder(String messageText) {
        super(messageText);
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
            onlinePlayer.sendMessage(new CustomMsg(this).get());
        }
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
            map.put("click-event-action", getClickEventAction().toString());
        }
        if (getClickEventValue() != null) {
            map.put("click-event-value", getClickEventValue());
        }
        return map;
    }

    public static BukkitMsgBuilder deserialize(Map<String, Object> map) {
        BukkitMsgBuilder msgBuilder = new BukkitMsgBuilder((String) map.get("message-text"));
        if (map.containsKey("hover-text")) {
            msgBuilder.hover((String) map.get("hover-text"));
        }
        if (map.containsKey("click-event-action") && map.containsKey("click-event-value")) {
            String action = (String) map.get("click-event-action");
            String value = (String) map.get("click-event-value");
            if (!action.isBlank() && !value.isBlank()) {
                msgBuilder.clickEvent(ClickEvent.Action.valueOf(action.toUpperCase(Locale.ROOT)), value);
            }
        }
        return msgBuilder;
    }
}
