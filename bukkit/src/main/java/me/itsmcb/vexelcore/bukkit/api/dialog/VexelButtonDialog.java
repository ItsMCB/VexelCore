package me.itsmcb.vexelcore.bukkit.api.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkitAPI;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VexelButtonDialog {

    private final Component title;
    private final List<DialogBody> bodyParts = new ArrayList<>();
    private final List<ButtonDef> buttonDefs = new ArrayList<>();

    private Component backLabel = new BukkitMsgBuilder("&c« Back").get(); // Todo localize
    private Component closeLabel = new BukkitMsgBuilder("&c» Close «").get(); // Todo localize

    private int columns = 3;

    private Consumer<Player> parentAction;

    public VexelButtonDialog(String title) {
        this.title = new BukkitMsgBuilder(title).get();
    }

    public VexelButtonDialog addBody(DialogBody body) {
        this.bodyParts.add(body);
        return this;
    }

    public VexelButtonDialog addMessage(String text) {
        this.bodyParts.add(DialogBody.plainMessage(new BukkitMsgBuilder(text).get(),600));
        return this;
    }

    public VexelButtonDialog addMessage(Component text) {
        this.bodyParts.add(DialogBody.plainMessage(text,600));
        return this;
    }

    public VexelButtonDialog addButton(String label, String tooltip, Consumer<Player> action) {
        this.buttonDefs.add(new ButtonDef(
                new BukkitMsgBuilder(label).get(),
                tooltip != null ? new BukkitMsgBuilder(tooltip).get() : null,
                action
        ));
        return this;
    }

    public VexelButtonDialog addButton(Component label, String tooltip, Consumer<Player> action) {
        this.buttonDefs.add(new ButtonDef(
                label,
                tooltip != null ? new BukkitMsgBuilder(tooltip).get() : null,
                action
        ));
        return this;
    }

    public VexelButtonDialog setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    public VexelButtonDialog setParent(Consumer<Player> parentAction) {
        this.parentAction = parentAction;
        return this;
    }

    /**
     * Builds the dialog and safely opens it for the player.
     */
    public void open(Player player) {
        VexelCoreBukkitAPI.getDialogManager().openDialog(player, () -> {

            List<ActionButton> finalButtons = new ArrayList<>();

            for (ButtonDef def : buttonDefs) {
                Key btnKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), event -> {
                    if (def.action != null) def.action.accept(player);
                });

                finalButtons.add(ActionButton.builder(def.label)
                        .tooltip(def.tooltip)
                        .action(DialogAction.customClick(btnKey, null))
                        .build());
            }

            ActionButton bottomButton;
            if (parentAction != null) {
                Key backKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), event -> {
                    parentAction.accept(player);
                });

                bottomButton = ActionButton.builder(backLabel)
                        .action(DialogAction.customClick(backKey, null))
                        .build();
            } else {
                bottomButton = ActionButton.builder(closeLabel).build();
            }

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(title)
                            .body(bodyParts)
                            .build()
                    )
                    .type(DialogType.multiAction(
                            finalButtons,
                            bottomButton,
                            columns
                    ))
            );
            player.showDialog(dialog);
        });
    }

    private record ButtonDef(Component label, Component tooltip, Consumer<Player> action) {}
}