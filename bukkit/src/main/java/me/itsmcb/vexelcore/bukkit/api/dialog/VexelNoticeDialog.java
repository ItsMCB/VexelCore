package me.itsmcb.vexelcore.bukkit.api.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkitAPI;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VexelNoticeDialog {
    private TextComponent label = Component.empty();
    private List<TextComponent> messages = new ArrayList<>();
    private ActionButton closeButton;

    public VexelNoticeDialog setLabel(String label) {
        return setLabel(new BukkitMsgBuilder(label).get());
    }

    public VexelNoticeDialog setLabel(TextComponent label) {
        this.label = label;
        return this;
    }

    public VexelNoticeDialog setMessage (String message) {
        return setMessage(new BukkitMsgBuilder(message).get());
    }

    public VexelNoticeDialog setMessage(TextComponent message) {
        return setMessages(List.of(message));
    }

    public VexelNoticeDialog setMessages(List<TextComponent> messages) {
        this.messages = messages;
        return this;
    }

    public VexelNoticeDialog addMessage(String message) {
        this.messages.add(new BukkitMsgBuilder(message).get());
        return this;
    }

    public VexelNoticeDialog setCloseButton(String s) {
        this.closeButton = ActionButton.builder(new BukkitMsgBuilder(s).get()).build();
        return this;
    }

    public VexelNoticeDialog setCloseButton(ActionButton closeButton) {
        this.closeButton = closeButton;
        return this;
    }

    private List<DialogBody> generateBody() {
        List<DialogBody> messagesAsDialogBodies = new ArrayList<>();
        messages.forEach(msg -> {
            messagesAsDialogBodies.add(DialogBody.plainMessage(msg));
        });
        return messagesAsDialogBodies;
    }

    public Dialog construct() {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(this.label)
                        .body(generateBody())
                        .build()
                )
                .type(
                        (closeButton == null ? DialogType.notice() : DialogType.notice(closeButton))
                )
        );
    }
    /**
     * Show the notice to the player.
     */
    public void show(@NotNull Player p) {
        VexelCoreBukkitAPI.getDialogManager().openDialog(p, () -> {
            p.showDialog(construct());
        });
    }
}