package me.itsmcb.vexelcore.common.api.text;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

public class CommonMsgBuilder {

    private String messageText;
    private String hoverText = null;
    private ClickEvent.Action clickEventAction = null;
    private String clickEventValue = null;

    public CommonMsgBuilder(@NotNull String messageText) {
        this.messageText = messageText;
    }

    public CommonMsgBuilder text(@NotNull String messageText) {
        this.messageText = messageText;
        return this;
    }

    public CommonMsgBuilder hover(@NotNull String hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public CommonMsgBuilder clickEvent(@NotNull ClickEvent.Action clickEventAction, @NotNull String clickEventValue) {
        System.out.println("Spain: " + clickEventAction.name() + " | " + clickEventValue);
        this.clickEventAction = clickEventAction;
        this.clickEventValue = clickEventValue;
        return this;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getHoverText() {
        return this.hoverText;
    }

    public ClickEvent.Action getClickEventAction() {
        return this.clickEventAction;
    }

    public String getClickEventValue() {
        return this.clickEventValue;
    }

    public CustomMsg build() {
        CustomMsg msg = new CustomMsg(this);
        return msg;
    }

    public TextComponent get() {
        return new CustomMsg(this).get();
    }

}
