package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class MenuV2 {
    private String title = "Untitled VexelCore API Menu";

    private InventoryType inventoryType = InventoryType.CHEST;

    private ArrayList<MenuV2Item> items = new ArrayList<>();

    private ArrayList<MenuV2Item> currentItems = new ArrayList<>();

    private ArrayList<MenuV2Item> staticItems = new ArrayList<>();

    private int page = 1;

    private int size = 0;

    private Inventory inventory;

    private UUID uuid = UUID.randomUUID();

    public MenuV2() {}

    public MenuV2(String title) {
        title(title);
        this.currentItems = items;
    }

    public MenuV2(String title, InventoryType inventoryType) {
        title(title);
        this.currentItems = items;
        this.inventoryType = inventoryType;
    }

    public MenuV2(String title, InventoryType inventoryType, int size) {
        title(title);
        this.currentItems = items;
        this.inventoryType = inventoryType;
        this.size = size;
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

    public MenuV2 addStaticItem(MenuV2Item menuItem) {
        this.staticItems.add(menuItem);
        return this;
    }

    public UUID getUUID() {
        return uuid;
    }

    public ArrayList<MenuV2Item> getItems() {
        return items;
    }

    public ArrayList<MenuV2Item> getStaticItems() {
        return staticItems;
    }

    public ArrayList<MenuV2Item> getCurrentItems() {
        return currentItems;
    }

    public void setCurrentItems(ArrayList<MenuV2Item> currentItems, Player player) {
        this.currentItems = currentItems;
        setInventoryItems(player);
    }

    public int getPage() {
        return page;
    }

    public int setPage(int page) {
        this.page = page;
        return page;
    }

    public int getSize() {
        return size;
    }

    public int staticItemsSize() {
        return staticItems.size();
    }

    public Inventory generateInventory() {
        // Create inventory and set UUID holder
        MenuHolder menuHolder = new MenuHolder(uuid);
        return Bukkit.createInventory(menuHolder, inventoryType, new BukkitMsgBuilder(getTitle()).get());
    }

    public Inventory generate(Player player) {
        inventory = generateInventory();
        setInventoryItems(player);
        return inventory;
    }

    public void setInventoryItems(Player player) {
        inventory.clear();
        // Set static inventory items
        staticItems.stream().forEach(item -> {
            inventory.setItem(item.getSlot(), item.getItemBuilder().getItemStack());
        });

        // Set inventory items with specified slots
        currentItems.stream().filter(item -> (item.getSlot() != -1)).sorted(Comparator.comparingInt(MenuV2Item::getSlot)).forEach(item -> {
            inventory.setItem(item.getSlot(), item.getItemBuilder().getItemStack());
        });
        // Set inventory items without any specified slots
        // Size is limited to prevent lag and unnecessary processing
        currentItems.stream().filter(item -> (item.getSlot() == -1)).limit(getSize()).forEach(item -> {
            inventory.addItem(item.getItemBuilder().getItemStack());
        });
    }

}
