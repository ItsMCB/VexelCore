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

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, new BukkitMsgBuilder(title).get());
        items.forEach((slot, item) -> inventory.setItem(slot, item.getItemStack()));
        player.openInventory(inventory);
    }
}
