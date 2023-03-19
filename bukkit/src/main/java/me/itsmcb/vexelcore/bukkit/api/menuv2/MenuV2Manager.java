package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MenuV2Manager implements Listener {

    public static NamespacedKey menuSystemIdKey = new NamespacedKey("vc-menu-system","vc-menu-item-id");

    private ArrayList<MenuV2> menus = new ArrayList<>();

    public MenuV2Manager(JavaPlugin instance) {
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void InventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) {
            return;
        }
        if (holder instanceof MenuHolder menuHolder) {
            try {
                menus.remove(getMenuFromUUID(menuHolder.getUUID()));
                System.out.println("Removed menu with UUID " + menuHolder.getUUID().toString());
            } catch (NoSuchElementException e) {
                // Do nothing
            }
        }
    }

    @EventHandler
    public void InventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) {
            return;
        }
        if (!(holder instanceof MenuHolder)) {
            return;
        }
        MenuHolder menuHolder = (MenuHolder) holder;
        MenuV2 menu;
        try {
            menu = getMenuFromUUID(menuHolder.getUUID());
        } catch (NoSuchElementException e) {
            // Not a valid menu
            return;
        }
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) {
            return;
        }
        Optional<MenuV2Item> optional = menu.getItems().stream().filter(item -> item.getUUID().equals(getMenuItemUUID(currentItem))).findFirst();
        if (optional.isEmpty()) {
            // Did not click a valid item
            return;
        }
        MenuV2Item item = optional.get();
        if (!item.isMovable()) {
            // Item is not movable, cancel take event
            event.setCancelled(true);
        }
        Player player = (Player) event.getWhoClicked();
        // Update player inventory to prevent client-side glitches
        player.updateInventory();
        if (event.getClick().isRightClick() && item.getRightClickAction() != null) {
            item.getRightClickAction().accept(event);
        }
        if (event.getClick().isLeftClick() && item.getLeftClickAction() != null) {
            item.getLeftClickAction().accept(event);
        }
    }

    private UUID getMenuItemUUID(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        if (!container.has(menuSystemIdKey)) {
            return null;
        }
        String foundUUID = container.get(menuSystemIdKey,PersistentDataType.STRING);
        if (foundUUID == null) {
            return null;
        }
        return UUID.fromString(foundUUID);
    }

    private MenuV2 getMenuFromUUID(UUID uuid) throws NoSuchElementException{
        Optional<MenuV2> optional = menus.stream().filter(menu -> menu.getUUID().equals(uuid)).findFirst();
        if (optional.isEmpty()) {
            throw new NoSuchElementException("Menu with UUID \""+uuid+"\" does not exist in stored list!");
        }
        return optional.get();
    }

    public void open(MenuV2 menu, Player player) {
        menus.add(menu);
        // Create inventory and set UUID holder
        Inventory inventory = Bukkit.createInventory(new MenuHolder(menu.getUUID()), menu.getInventoryType(), new BukkitMsgBuilder(menu.getTitle()).get());

        // TODO check that items can fit inside inventory screen

        // Set inventory items with specified slots
        menu.getItems().stream().filter(item -> (item.getSlot() != -1)).sorted(Comparator.comparingInt(MenuV2Item::getSlot)).forEach(item -> {
            inventory.setItem(item.getSlot(), item.getItemBuilder().getItemStack());
        });

        // Set inventory items without any specified slots

        menu.getItems().stream().filter(item -> (item.getSlot() == -1)).forEach(item -> {
            inventory.addItem(item.getItemBuilder().getItemStack());
        });

        player.openInventory(inventory);
    }

}
