package me.itsmcb.vexelcore.bukkit.api.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkitAPI;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.bukkit.api.utils.ChatUtils;
import me.itsmcb.vexelcore.common.api.HeadTexture;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class VexelPaginatedDialog<T> {

    private final String title;
    private final List<DialogBody> bodyParts = new ArrayList<>();

    private Component forwardLabel = new BukkitMsgBuilder("&fNext Page").get(); // Todo localize
    private Component backwardLabel = new BukkitMsgBuilder("&fPrevious Page").get(); // Todo localize
    private Component backLabel = new BukkitMsgBuilder("&c« Back").get(); // Todo localize
    private Component closeLabel = new BukkitMsgBuilder("&c» Close «").get(); // Todo localize

    // Defaults
    private int itemsPerPage = 10;
    private int columns = 2;

    private final List<T> items;
    private Function<T, CompletableFuture<ActionButton>> itemRenderer;
    private Consumer<Player> parentAction;

    public VexelPaginatedDialog(String title, List<T> items) {
        this.title = title;
        this.items = items;
    }

    public VexelPaginatedDialog<T> setRenderer(Function<T, CompletableFuture<ActionButton>> renderer) {
        this.itemRenderer = renderer;
        return this;
    }

    public VexelPaginatedDialog<T> addMessage(String text) {
        this.bodyParts.add(DialogBody.plainMessage(new BukkitMsgBuilder(text).get(),600));
        return this;
    }

    public VexelPaginatedDialog<T> addMessage(Component component) {
        this.bodyParts.add(DialogBody.plainMessage(component,600));
        return this;
    }

    public VexelPaginatedDialog<T> addBody(DialogBody body) {
        this.bodyParts.add(body);
        return this;
    }

    public VexelPaginatedDialog<T> setParent(Consumer<Player> parentAction) {
        this.parentAction = parentAction;
        return this;
    }

    public VexelPaginatedDialog<T> setItemsPerPage(int amount) {
        this.itemsPerPage = amount;
        return this;
    }

    public VexelPaginatedDialog<T> setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    /**
     * Builds the dialog and safely opens it for the player. Starts at the first page.
     */
    public void open(Player player) {
        open(player, 0);
    }

    /**
     * Builds the dialog and safely opens it for the player. Starts at the provided page number.
     */
    private void open(Player player, int page) {
        // Calculations
        int totalItems = items.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        final int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        List<T> pageItems = (totalItems > 0) ? items.subList(startIndex, endIndex) : new ArrayList<>();

        // Start Rendering Async
        List<CompletableFuture<ActionButton>> futures = pageItems.stream()
                .map(item -> itemRenderer.apply(item))
                .toList();

        // Join
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
            List<ActionButton> renderedButtons = futures.stream().map(CompletableFuture::join).toList();

            // Build UI on Main Thread
            Bukkit.getScheduler().runTask(VexelCoreBukkit.getInstance(), () -> {
                buildAndShow(player, renderedButtons, currentPage, totalPages);
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    private void buildAndShow(Player player, List<ActionButton> contentButtons, int page, int totalPages) {
        VexelCoreBukkitAPI.getDialogManager().openDialog(player, () -> {

            List<ActionButton> finalActions = new ArrayList<>(contentButtons);

            // Navigation logic. Append Previous/Next buttons only if relevant.
            if (totalPages > 1) {
                // Previous Button
                if (page > 0) {
                    Key prevKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> open(player, page - 1));

                    finalActions.add(ActionButton.builder(ChatUtils.makeLabel(HeadTexture.GRAY_ARROW_LEFT,backwardLabel))
                            .action(DialogAction.customClick(prevKey, null))
                            .build());
                }
                // Next Button
                if (page < totalPages - 1) {
                    Key nextKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> open(player, page + 1));

                    finalActions.add(ActionButton.builder(ChatUtils.makeLabel(HeadTexture.GRAY_ARROW_RIGHT,forwardLabel))
                            .action(DialogAction.customClick(nextKey, null))
                            .build());
                }
            }

            // Footer (Back/Close)
            ActionButton footerBtn;
            if (parentAction != null) {
                Key backKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> parentAction.accept(player));

                footerBtn = ActionButton.builder(backLabel)
                        .action(DialogAction.customClick(backKey, null))
                        .build();
            } else {
                footerBtn = ActionButton.builder(closeLabel).build();
            }

            // Page Indicator
            List<DialogBody> finalBody = new ArrayList<>(bodyParts);
            if (totalPages > 1) {
                finalBody.add(DialogBody.plainMessage(new BukkitMsgBuilder("&8Page " + (page + 1) + "/" + totalPages).get()));
            }

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(new BukkitMsgBuilder(title).get())
                            .body(finalBody)
                            .build())
                    .type(DialogType.multiAction(finalActions, footerBtn, columns))
            );

            player.showDialog(dialog);
        });
    }
}