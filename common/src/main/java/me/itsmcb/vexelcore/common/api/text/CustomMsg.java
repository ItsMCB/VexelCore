package me.itsmcb.vexelcore.common.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CustomMsg {

    private String messageText;
    private String hoverText;
    private ClickEvent.Action clickEventAction;
    private String clickEventValue;

    public CustomMsg(@NotNull CommonMsgBuilder builder) {
        this.messageText = builder.getMessageText();
        this.hoverText = builder.getHoverText();
        this.clickEventAction = builder.getClickEventAction();
        this.clickEventValue = builder.getClickEventValue();
    }

    public @NotNull TextComponent get() {
        TextComponent.Builder component = Component.text().content("").append(componentize(messageText != null ? messageText : ""));
        if (hoverText != null) {
            component.hoverEvent(HoverEvent.showText(componentize(hoverText)));
        }
        if (clickEventAction != null && clickEventValue != null) {
            if (Objects.equals(clickEventAction, ClickEvent.Action.SUGGEST_COMMAND)) {
                component.clickEvent(ClickEvent.suggestCommand(clickEventValue));
            } else if (Objects.equals(clickEventAction, ClickEvent.Action.RUN_COMMAND)) {
                component.clickEvent(ClickEvent.runCommand(clickEventValue));
            } else if (Objects.equals(clickEventAction, ClickEvent.Action.OPEN_URL)) {
                component.clickEvent(ClickEvent.openUrl(clickEventValue));
            } else if (Objects.equals(clickEventAction, ClickEvent.Action.COPY_TO_CLIPBOARD)) {
                component.clickEvent(ClickEvent.copyToClipboard(clickEventValue));
            } else if (Objects.equals(clickEventAction, ClickEvent.Action.OPEN_FILE)) {
                component.clickEvent(ClickEvent.openFile(clickEventValue));
            } else if (Objects.equals(clickEventAction, ClickEvent.Action.CHANGE_PAGE)) {
                try {
                    component.clickEvent(ClickEvent.changePage(Integer.parseInt(clickEventValue)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return component.build();
    }

    // Internal

    private Component componentize(final @NotNull String string) {
        return LegacyComponentSerializer.legacy('&').deserialize(string);
    }

}