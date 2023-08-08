package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class MenuV2 {
    private String title = "Untitled VexelCore API Menu";

    private InventoryType inventoryType = InventoryType.CHEST;

    private ArrayList<MenuV2Item> items = new ArrayList<>();

    private ArrayList<MenuV2Item> currentItems = new ArrayList<>();

    private ArrayList<MenuV2Item> staticItems = new ArrayList<>();

    private int page = 1;

    private int size = 54;

    private Inventory inventory;

    private UUID uuid = UUID.randomUUID();

    boolean clickClosesMenu = false;

    private MenuV2 previousMenu = null;
    private MenuV2Manager manager = null;

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

    public void setPreviousMenu(MenuV2 previousMenu) {
        this.previousMenu = previousMenu;
    }

    public MenuV2 getPreviousMenu() {
        return previousMenu;
    }

    public void setManager(MenuV2Manager manager) {
        this.manager = manager;
    }

    public MenuV2Manager getManager() {
        return manager;
    }

    public MenuV2 createNewCopy(Player player) {
        // Reset IDs for items and static items to not conflict with other menus
        this.items = new ArrayList<>(items.stream().map(MenuV2Item::createNewCopy).toList());
        this.staticItems = new ArrayList<>(staticItems.stream().map(MenuV2Item::createNewCopy).toList());
        // Refresh inv
        generate(player);
        return this;
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
        // Remove if slot is already taken
        if (menuItem.getSlot() != -1) {
            Optional<MenuV2Item> optionalItem = items.stream().filter(item -> item.getSlot() == menuItem.getSlot()).findFirst();
            optionalItem.ifPresent(menuV2Item -> items.remove(menuV2Item));
        }
        this.items.add(menuItem);
        return this;
    }

    public MenuV2 addStaticItem(MenuV2Item menuItem) {
        // Remove if slot is already taken
        if (menuItem.getSlot() != -1) {
            Optional<MenuV2Item> optionalItem = staticItems.stream().filter(item -> item.getSlot() == menuItem.getSlot()).findFirst();
            optionalItem.ifPresent(menuV2Item -> staticItems.remove(menuV2Item));
        }
        // Add
        this.staticItems.add(menuItem);
        return this;
    }

    public UUID getUUID() {
        return uuid;
    }

    public MenuV2 clickCloseMenu(boolean bool) {
        this.clickClosesMenu = bool;
        return this;
    }

    public boolean shouldClickCloseMenu() {
        return clickClosesMenu;
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
        MenuHolder menuHolder = getMenuHolder();
        if (inventoryType.equals(InventoryType.CHEST)) {
            return Bukkit.createInventory(menuHolder, size, new BukkitMsgBuilder(getTitle()).get());
        }
        return Bukkit.createInventory(menuHolder, inventoryType, new BukkitMsgBuilder(getTitle()).get());
    }

    public MenuHolder getMenuHolder() {
        return new MenuHolder(uuid);
    }

    public Inventory generate(Player player) {
        inventory = generateInventory();
        setCurrentItems(new ArrayList<>(getItems().subList(0, Math.min(inventory.getSize(), items.size()))), player);
        return inventory;
    }

    public void setInventoryItems(Player player) {
        inventory.clear();
        // Set static inventory items
        staticItems.stream().forEach(item -> {
            if (item.shouldUpdate()) {
                item.update();
            }
            inventory.setItem(item.getSlot(), item);
        });

        // Set inventory items with specified slots
        currentItems.stream().filter(item -> (item.getSlot() != -1)).sorted(Comparator.comparingInt(MenuV2Item::getSlot)).toList().forEach(item -> {
            if (item.shouldUpdate()) {
                item.update();
            }
            inventory.setItem(item.getSlot(), item);
        });
        // Set inventory items without any specified slots
        // Size is limited to prevent lag and unnecessary processing
        currentItems.stream().filter(item -> (item.getSlot() == -1)).limit(getSize()).forEach(item -> {
            if (item.shouldUpdate()) {
                item.update();
            }
            inventory.addItem(item);
        });
    }

}
