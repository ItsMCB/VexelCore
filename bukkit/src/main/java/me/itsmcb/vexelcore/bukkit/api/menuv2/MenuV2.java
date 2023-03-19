package me.itsmcb.vexelcore.bukkit.api.menuv2;

import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.UUID;

public class MenuV2 {
    private String title = "Untitled VexelCore API Menu";

    private InventoryType inventoryType = InventoryType.CHEST;

    private ArrayList<MenuV2Item> items = new ArrayList<>();

    private UUID uuid = UUID.randomUUID();

    public MenuV2() {}

    public MenuV2(String title) {
        this.title = title;
    }

    public MenuV2(String title, InventoryType inventoryType) {
        this.title = title;
        this.inventoryType = inventoryType;
    }

    public MenuV2 title(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MenuV2 type(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
        return this;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public MenuV2 addItem(MenuV2Item menuItem) {
        this.items.add(menuItem);
        return this;
    }

    public UUID getUUID() {
        return uuid;
    }

    public ArrayList<MenuV2Item> getItems() {
        return items;
    }

}
