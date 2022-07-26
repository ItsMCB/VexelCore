package me.itsmcb.vexelcore.bukkit.api.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;

public class MsgBuilder {

    private String messageText;
    private String hoverText = null;
    private ClickEvent.Action clickEventAction = null;
    private String clickEventValue = null;


    public MsgBuilder(String messageText) {
        this.messageText = messageText;
    }

    public MsgBuilder hover(String hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public MsgBuilder clickEvent(ClickEvent.Action clickEventAction, String clickEventValue) {
        this.clickEventAction = clickEventAction;
        this.clickEventValue = clickEventValue;
        return this;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getHoverText() {
        return hoverText;
    }

    public ClickEvent.Action getClickEventAction() {
        return clickEventAction;
    }

    public String getClickEventValue() {
        return clickEventValue;
    }

    public CustomMsg build() {
        CustomMsg msg = new CustomMsg(this);
        return msg;
    }

    // Shortcuts

    public void send(CommandSender sender) {
        new CustomMsg(this).send(sender);
    }

    public void sendAll() {
        new CustomMsg(this).sendAll();
    }

    public TextComponent get() {
        return new CustomMsg(this).get();
    }
}
