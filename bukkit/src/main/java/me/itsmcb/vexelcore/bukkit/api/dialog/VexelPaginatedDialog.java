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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Paginated dialog helper built on VexelCore's {@code DialogSessionManager}.
 * Resolves async button templates first, then registers callbacks inside
 * {@code openDialog} so {@code startNewSession} never discards content callbacks.
 */
public class VexelPaginatedDialog<T> {

    public record ButtonEntry(
            @NotNull Component label,
            @Nullable Component tooltip,
            @Nullable Consumer<Player> action
    ) {
        public static @NotNull ButtonEntry of(@NotNull Component label, @Nullable String tooltip, @Nullable Consumer<Player> action) {
            return new ButtonEntry(label, tooltip != null ? new BukkitMsgBuilder(tooltip).get() : null, action);
        }

        public static @NotNull ButtonEntry of(@NotNull String label, @Nullable String tooltip, @Nullable Consumer<Player> action) {
            return new ButtonEntry(new BukkitMsgBuilder(label).get(), tooltip != null ? new BukkitMsgBuilder(tooltip).get() : null, action);
        }
    }

    private final Plugin plugin;
    private final String title;
    private final List<T> items;
    private final List<DialogBody> bodyParts = new ArrayList<>();

    private Component forwardLabel = new BukkitMsgBuilder("&fNext Page").get(); // Todo localize
    private Component backwardLabel = new BukkitMsgBuilder("&fPrevious Page").get(); // Todo localize
    private Component backLabel = new BukkitMsgBuilder("&c« Back").get(); // Todo localize
    private Component closeLabel = new BukkitMsgBuilder("&c» Close «").get(); // Todo localize

    // Defaults
    private int itemsPerPage = 10;
    private int columns = 2;
    private String emptyMessage = "&7Nothing to display.";

    private Function<T, CompletableFuture<ButtonEntry>> entryRenderer;
    private Consumer<Player> parentAction;
    private boolean showCloseButton = true;

    public VexelPaginatedDialog(@NotNull String title, @NotNull List<T> items) {
        this(VexelCoreBukkit.getInstance(), title, items);
    }

    public VexelPaginatedDialog(@NotNull Plugin plugin, @NotNull String title, @NotNull List<T> items) {
        this.plugin = plugin;
        this.title = title;
        this.items = items;
    }

    public VexelPaginatedDialog<T> setRenderer(@NotNull Function<T, ButtonEntry> renderer) {
        this.entryRenderer = item -> CompletableFuture.completedFuture(renderer.apply(item));
        return this;
    }

    public VexelPaginatedDialog<T> setAsyncRenderer(@NotNull Function<T, CompletableFuture<ButtonEntry>> renderer) {
        this.entryRenderer = renderer;
        return this;
    }

    public VexelPaginatedDialog<T> addMessage(@NotNull String text) {
        this.bodyParts.add(DialogBody.plainMessage(new BukkitMsgBuilder(text).get(), 600));
        return this;
    }

    public VexelPaginatedDialog<T> addMessage(@NotNull Component component) {
        this.bodyParts.add(DialogBody.plainMessage(component, 600));
        return this;
    }

    public VexelPaginatedDialog<T> addBody(@NotNull DialogBody body) {
        this.bodyParts.add(body);
        return this;
    }

    public VexelPaginatedDialog<T> setEmptyMessage(@NotNull String emptyMessage) {
        this.emptyMessage = emptyMessage;
        return this;
    }

    public VexelPaginatedDialog<T> setParent(@Nullable Consumer<Player> parentAction) {
        this.parentAction = parentAction;
        return this;
    }

    public VexelPaginatedDialog<T> setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
        return this;
    }

    public VexelPaginatedDialog<T> setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = Math.max(1, itemsPerPage);
        return this;
    }

    public VexelPaginatedDialog<T> setColumns(int columns) {
        this.columns = Math.max(1, columns);
        return this;
    }

    /**
     * Builds the dialog and safely opens it for the player. Starts at the first page.
     */
    public void open(@NotNull Player player) {
        open(player, 0);
    }

    /**
     * Builds the dialog and safely opens it for the player. Starts at the provided page number.
     */
    private void open(@NotNull Player player, int page) {
        if (items.isEmpty()) {
            showEmptyState(player);
            return;
        }

        // Calculations
        int totalItems = items.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        List<T> pageItems = items.subList(startIndex, endIndex);

        // Start Rendering Async
        List<CompletableFuture<ButtonEntry>> futures = pageItems.stream()
                .map(entryRenderer)
                .toList();

        // Join
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
            List<ButtonEntry> entries = futures.stream().map(CompletableFuture::join).toList();

            // Build UI on Main Thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    buildAndShow(player, entries, currentPage, totalPages);
                }
            });
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.WARNING, "Failed to render paginated dialog '" + title + "'", ex);
            return null;
        });
    }

    private void showEmptyState(@NotNull Player player) {
        if (parentAction != null) {
            VexelCoreBukkitAPI.getDialogManager().openDialog(player, () -> {
                Key backKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> parentAction.accept(player));
                ActionButton backBtn = ActionButton.builder(backLabel)
                        .action(DialogAction.customClick(backKey, null))
                        .build();
                Dialog dialog = Dialog.create(builder -> builder.empty()
                        .base(DialogBase.builder(new BukkitMsgBuilder(title).get())
                                .body(List.of(DialogBody.plainMessage(new BukkitMsgBuilder(emptyMessage).get(), 600)))
                                .build())
                        .type(DialogType.notice(backBtn))
                );
                player.showDialog(dialog);
            });
            return;
        }
        new VexelNoticeDialog()
                .setLabel(title)
                .setMessage(emptyMessage)
                .show(player);
    }

    private void buildAndShow(@NotNull Player player,
                              @NotNull List<ButtonEntry> entries,
                              int page,
                              int totalPages) {
        VexelCoreBukkitAPI.getDialogManager().openDialog(player, () -> {
            List<ActionButton> finalActions = new ArrayList<>();

            for (ButtonEntry entry : entries) {
                Key btnKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> {
                    if (entry.action() != null) {
                        entry.action().accept(player);
                    }
                });
                finalActions.add(ActionButton.builder(entry.label())
                        .tooltip(entry.tooltip())
                        .action(DialogAction.customClick(btnKey, null))
                        .build());
            }

            // Navigation logic. Append Previous/Next buttons only if relevant.
            if (totalPages > 1) {
                // Previous Button
                if (page > 0) {
                    Key prevKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> open(player, page - 1));
                    finalActions.add(ActionButton.builder(ChatUtils.makeLabel(HeadTexture.GRAY_ARROW_LEFT, backwardLabel))
                            .action(DialogAction.customClick(prevKey, null))
                            .build());
                }
                // Next Button
                if (page < totalPages - 1) {
                    Key nextKey = VexelCoreBukkitAPI.getDialogManager().addCallback(player.getUniqueId(), e -> open(player, page + 1));
                    finalActions.add(ActionButton.builder(ChatUtils.makeLabel(HeadTexture.GRAY_ARROW_RIGHT, forwardLabel))
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

                // If this dialog has a parent, also add a dedicated Close button so players
                // in deep menus can dismiss the dialog immediately without clicking Back repeatedly.
                if (showCloseButton) {
                    finalActions.add(ActionButton.builder(closeLabel).build());
                }
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