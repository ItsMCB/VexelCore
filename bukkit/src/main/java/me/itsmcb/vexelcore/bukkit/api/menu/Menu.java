package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom chest menu. {@link MenuButton}s are automatically refreshed and arranged.
 */
public class Menu implements InventoryHolder {

    public Inventory inventory;
    private MenuRowSize size;
    private String title;
    private boolean clickToClose = true;
    private boolean persistent = false;
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
        refreshRunnable.runTaskTimer(Bukkit.getPluginManager().getPlugin("VexelCore"), 0, 20);
    }

    /* Inventory */

    private void updateInventory() {
        this.inventory = Bukkit.createInventory(this,size.getSize(),new BukkitMsgBuilder(title).get());
    }

    public void setSize(MenuRowSize size) {
        this.size = size;
        updateInventory();
    }

    public MenuRowSize getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateInventory();
    }

    public Menu open(Player player) {
        player.openInventory(getInventory());
        return this;
    }

    /**
     * Update each menu item and set them into the inventory.
     */
    public void refresh() {
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

    /**
     * Prepare menu to be destroyed by cancelling the runnable refreshing {@link MenuButton}s.
     */
    public void unload() {
        refreshRunnable.cancel();
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
        return this;
    }

    public HashMap<Integer, MenuButton> getPositionedButtons() {
        return positionedButtons;
    }

    public Menu addButton(int index, @NotNull MenuButton button) {
        positionedButtons.put(index,button);
        return this;
    }

    public ArrayList<MenuButton> getUnpositionedButtons() {
        return unpositionedButtons;
    }

    public Menu addButton(@NotNull MenuButton button) {
        unpositionedButtons.add(button);
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
