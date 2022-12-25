package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class MenuManager implements Listener {

    private HashMap<String,Menu> menus = new HashMap<>();
    private JavaPlugin instance;
    private NamespacedKey menuItemKey;
    private NamespacedKey removeableKey;
    private NamespacedKey menuItemActionRightKey;
    private NamespacedKey menuItemActionLeftKey;

    public MenuManager(JavaPlugin instance) {
        this.instance = instance;
        this.menuItemKey =  new NamespacedKey(instance, "vc-menu-item-id");
        this.removeableKey =  new NamespacedKey(instance, "vc-menu-item-removable");
        this.menuItemActionRightKey =  new NamespacedKey(instance, "vc-menu-button-right");
        this.menuItemActionLeftKey =  new NamespacedKey(instance, "vc-menu-button-left");
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public NamespacedKey getMenuItemKey() {
        return menuItemKey;
    }

    public NamespacedKey getMenuItemActionLeftKey() {
        return menuItemActionLeftKey;
    }

    public NamespacedKey getMenuItemActionRightKey() {
        return menuItemActionRightKey;
    }

    public JavaPlugin getInstance() {
        return instance;
    }

    public void setMenu(String menuId, Menu menu) {
        this.menus.put(menuId, menu);
    }

    public Menu getMenu(String menuId) {
        if (!menus.containsKey(menuId)) {
            return null;
        }
        return this.menus.get(menuId);
    }

    public void open(Player player, String menuId) {
        open(player, menuId, 1);
    }

    public void open(Player player, String menuId, int pageNumber) {
        // todo check if menu exists and if page exists
        Menu menu = getMenu(menuId);
        menu.openPage(player, pageNumber);
    }

    @EventHandler
    public void InventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (!isMenuItem(clickedItem)) {
            return;
        }
        PersistentDataContainer clickedItemContainer = clickedItem.getItemMeta().getPersistentDataContainer();
        // By default, a valid menu item can't be taken and will close the inventory once clicked.
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        player.updateInventory();
        player.closeInventory();
        String clickedItemMenuId = clickedItemContainer.get(menuItemKey, PersistentDataType.STRING);
        MenuItem foundItem = findItemWithId(clickedItemMenuId);
        if (foundItem == null) {
            return;
        }
        // Right click
        if (hasKey(clickedItem, menuItemActionRightKey) && hasKey(foundItem.getItemStack(), menuItemActionRightKey)) {
            if (event.getClick().isRightClick()) {
                foundItem.getRightClickAction().accept(event);
            }
        }
        // Left click
        if (hasKey(clickedItem, menuItemActionLeftKey) && hasKey(foundItem.getItemStack(), menuItemActionLeftKey)) {
            if (event.getClick().isLeftClick()) {
                foundItem.getLeftClickAction().accept(event);
            }
        }
        // Let item be moved
        if (clickedItemContainer.has(removeableKey)) {
            event.setCancelled(false);
        }
    }

    /* Leaving uncommented as it doesn't seem useful as expected event fires in event above
    @EventHandler
    public void InventoryClick(InventoryMoveItemEvent event) {
        ItemStack clickedItem = event.getItem();
        if (!isMenuItem(clickedItem)) {
            return;
        }
        event.setCancelled(true);
        PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();
        if (container.has(removeableKey)) {
            event.setCancelled(false);
        }
        System.out.println("EVENT: " + event.getEventName() + " | Cancelled?: " + event.isCancelled());
    }

     */

    private boolean isMenuItem(ItemStack itemStack) {
        // Validate item before checking data
        if (!itemHasMeta(itemStack)) {
            return false;
        }
        return hasKey(itemStack, menuItemKey);
    }

    private boolean hasKey(ItemStack itemStack, NamespacedKey key) {
        if (!itemHasMeta(itemStack)) {
            return false;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        if (container.isEmpty()) {
            return false;
        }
        return container.has(key);
    }

    private boolean itemHasMeta(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        ItemMeta clickedItemMeta = itemStack.getItemMeta();
        return (clickedItemMeta != null);
    }

    public MenuItem findItemWithId(String id) {
        AtomicReference<MenuItem> menuItem = new AtomicReference<>(null);
        menus.forEach((key, value) -> value.getPages().forEach((integer, page) -> page.getItems().forEach((slot, item) -> {
            if (item.getId().toString().equalsIgnoreCase(id)) {
                menuItem.set(item);
            }
        })));
        return menuItem.get();
    }

    public boolean itemStackHasKey(ItemStack itemStack, String key) {
        if (itemStack == null) {
            return false;
        }
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.has(NamespacedKey.fromString(key,instance));
    }
}
