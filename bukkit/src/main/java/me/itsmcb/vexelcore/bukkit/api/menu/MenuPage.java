package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class MenuPage {

    String title;
    HashMap<Integer, MenuItem> items = new HashMap<>();

    public MenuPage(String title) {
        this.title = title;
    }

    public MenuPage(String title, HashMap<Integer, MenuItem> items) {
        this.title = title;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public HashMap<Integer, MenuItem> getItems() {
        return items;
    }

    public void addItem(int slot, MenuItem item) {
        items.put(slot, item);
    }

    public void open(Player player, MenuManager menuManager) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, new BukkitMsgBuilder(title).get());
        // Set item metadata
        items.forEach((slot, item) -> {
            item.setKey(menuManager.getMenuItemKey(), item.getId().toString());
            if (item.getRightClickAction() != null) {
                item.setKey(menuManager.getMenuItemActionRightKey(), "yes");
            }
            if (item.getLeftClickAction() != null) {
                item.setKey(menuManager.getMenuItemActionLeftKey(), "yes");
            }
            inventory.setItem(slot, item.getItemStack());
        });
        player.openInventory(inventory);
    }
}
