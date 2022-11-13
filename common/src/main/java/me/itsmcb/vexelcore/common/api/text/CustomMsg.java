package me.itsmcb.vexelcore.common.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

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
        TextComponent.Builder component = Component.text().content("").append(componentize(messageText));
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
        return LegacyComponentSerializer.legacy('&').deserialize(string);
    }

}
