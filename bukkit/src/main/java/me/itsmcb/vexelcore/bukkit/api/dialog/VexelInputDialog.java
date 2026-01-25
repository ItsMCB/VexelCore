package me.itsmcb.vexelcore.bukkit.api.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkitAPI;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VexelInputDialog {

    private final Component title;
    private final List<DialogBody> bodyParts = new ArrayList<>();
    private final List<DialogInput> inputs = new ArrayList<>();

    private Component confirmLabel = new BukkitMsgBuilder("&a» Confirm «").get(); // Todo localize
    private Component cancelLabel = new BukkitMsgBuilder("&c» Cancel «").get(); // Todo localize

    private Consumer<DialogContext> onConfirm;
    private Consumer<DialogContext> onCancel;

    public VexelInputDialog(String title) {
        this.title = new BukkitMsgBuilder(title).get();
    }

    public VexelInputDialog addMessage(String text) {
        this.bodyParts.add(DialogBody.plainMessage(new BukkitMsgBuilder(text).get()));
        return this;
    }

    public VexelInputDialog addMessage(Component text) {
        this.bodyParts.add(DialogBody.plainMessage(text,500));
        return this;
    }

    public VexelInputDialog addInput(DialogInput input) {
        this.inputs.add(input);
        return this;
    }

    public VexelInputDialog setConfirmButton(String text) {
        this.confirmLabel = new BukkitMsgBuilder(text).get();
        return this;
    }

    public VexelInputDialog setCancelButton(String text) {
        this.cancelLabel = new BukkitMsgBuilder(text).get();
        return this;
    }

    public VexelInputDialog onConfirm(Consumer<DialogContext> callback) {
        this.onConfirm = callback;
        return this;
    }

    public VexelInputDialog onCancel(Consumer<DialogContext> callback) {
        this.onCancel = callback;
        return this;
    }

    /**
     * Builds the dialog and safely opens it for the player.
     */
    public void open(Player player) {
        VexelCoreBukkitAPI.getDialogManager().openDialog(player, () -> {

            Key confirmKey;
            if (onConfirm != null) {
                confirmKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), event -> {
                    DialogContext ctx = new DialogContext(player, event.getDialogResponseView());
                    onConfirm.accept(ctx);
                });
            } else {
                confirmKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> {});
            }

            Key cancelKey = null;
            if (onCancel != null) {
                cancelKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), event -> {
                    DialogContext ctx = new DialogContext(player, event.getDialogResponseView());
                    onCancel.accept(ctx);
                });
            }

            DialogAction cancelAction = (cancelKey != null)
                    ? DialogAction.customClick(cancelKey, null)
                    : null;

            ActionButton confirmBtn = ActionButton.builder(confirmLabel)
                    .action(DialogAction.customClick(confirmKey, null))
                    .build();

            ActionButton cancelBtn = ActionButton.builder(cancelLabel)
                    .action(cancelAction)
                    .build();

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(title)
                            .body(bodyParts)
                            .inputs(inputs)
                            .build()
                    )
                    .type(DialogType.confirmation(confirmBtn, cancelBtn))
            );

            player.showDialog(dialog);
        });
    }
}