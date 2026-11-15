package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * Custom chest menu. {@link MenuButton}s are automatically refreshed and arranged.
 */
public class Menu implements InventoryHolder {

    public Inventory inventory;
    private MenuRowSize size;
    private InventoryType inventoryType;
    private String title;
    private boolean clickToClose = true;
    private boolean persistent = false;
    private boolean menuUpdateRequested = false;
    private boolean refreshTaskStarted = false;
    private BukkitRunnable refreshRunnable = new BukkitRunnable() {
        @Override
        public void run() {
            refresh();
        }
    };
    private HashMap<Integer, MenuButton> templatePositionedButtons = new HashMap<>(); // Index applied to each page
    private HashMap<Integer, MenuButton> positionedButtons = new HashMap<>(); // Buttons may appear on different pages
    private ArrayList<MenuButton> unpositionedButtons = new ArrayList<>(); // Buttons added to empty space

    public Menu(MenuRowSize size, String title) {
        this.size = size;
        this.title = title;
        updateInventory();
    }

    public Menu(InventoryType inventoryType, String title) {
        this.inventoryType = inventoryType;
        this.title = title;
        updateInventory();
    }

    /* Inventory */

    private void updateInventory() {
        if (inventoryType != null) {
            this.inventory = Bukkit.createInventory(this, inventoryType, new BukkitMsgBuilder(title).get());
        } else {
            MenuRowSize targetSize = size != null ? size : MenuRowSize.THREE;
            this.inventory = Bukkit.createInventory(this, targetSize.getSize(), new BukkitMsgBuilder(title).get());
        }
        setMenuUpdateRequested(true);
    }

    public void setSize(MenuRowSize size) {
        this.size = size;
        this.inventoryType = null;
        updateInventory();
    }

    public MenuRowSize getSize() {
        return size;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
        updateInventory();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateInventory();
    }

    public Menu open(Player player) {
        startRefreshTaskIfNeeded();
        refresh();
        player.openInventory(getInventory());
        return this;
    }

    private void startRefreshTaskIfNeeded() {
        if (!refreshTaskStarted) {
            refreshTaskStarted = true;
            refreshRunnable.runTaskTimer(Bukkit.getPluginManager().getPlugin("VexelCore"), 20L, 20L);
        }
    }

    /**
     * Update each menu item and set them into the inventory.
     */
    public void refresh() {
        Optional<MenuButton> templatePosition = templatePositionedButtons.values().stream().filter(MenuButton::isButtonUpdateRequested).findFirst();
        Optional<MenuButton> positioned = positionedButtons.values().stream().filter(MenuButton::isButtonUpdateRequested).findFirst();
        Optional<MenuButton> unpositioned = unpositionedButtons.stream().filter(MenuButton::isButtonUpdateRequested).findFirst();
        if (menuUpdateRequested || templatePosition.isPresent() || positioned.isPresent() || unpositioned.isPresent()) {
            rebuildMenu();
            setMenuUpdateRequested(false);
        }
    }

    public void rebuildMenu() {
        inventory.clear();
        // Refresh and get
        templatePositionedButtons.forEach((i,b) -> {
            inventory.setItem(i,b.refresh().get());
        });
        positionedButtons.forEach((i,b) -> {
            inventory.setItem(i,b.refresh().get());
        });
        unpositionedButtons.forEach(b -> inventory.addItem(b.refresh().get()));
    }

    public void setMenuUpdateRequested(boolean menuUpdateRequested) {
        this.menuUpdateRequested = menuUpdateRequested;
    }

    public boolean isMenuUpdateRequested() {
        return menuUpdateRequested;
    }

    /**
     * Prepare menu to be destroyed by cancelling the runnable refreshing {@link MenuButton}s.
     */
    public void unload() {
        if (refreshTaskStarted && !refreshRunnable.isCancelled()) {
            refreshRunnable.cancel();
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /* Buttons */

    public HashMap<Integer, MenuButton> getTemplatePositionedButtons() {
        return templatePositionedButtons;
    }

    public Menu setTemplateButton(int index, @NotNull MenuButton button) {
        templatePositionedButtons.put(index,button);
        setMenuUpdateRequested(true);
        return this;
    }

    public HashMap<Integer, MenuButton> getPositionedButtons() {
        return positionedButtons;
    }

    public Menu addButton(int index, @NotNull MenuButton button) {
        positionedButtons.put(index,button);
        setMenuUpdateRequested(true);
        return this;
    }

    public ArrayList<MenuButton> getUnpositionedButtons() {
        return unpositionedButtons;
    }

    public Menu addButton(@NotNull MenuButton button) {
        unpositionedButtons.add(button);
        setMenuUpdateRequested(true);
        return this;
    }

    /* MenuManager handling settings */

    public Menu clickToClose(boolean clickToClose) {
        this.clickToClose = clickToClose;
        return this;
    }

    public boolean isClickToClose() {
        return clickToClose;
    }

    public Menu persistence(boolean bool) {
        this.persistent = bool;
        return this;
    }

    public boolean isPersistent() {
        return persistent;
    }
}