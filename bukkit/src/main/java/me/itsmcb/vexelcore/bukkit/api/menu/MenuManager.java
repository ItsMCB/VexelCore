package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class MenuManager implements Listener {

    HashMap<String,Menu> menus = new HashMap<>();
    JavaPlugin instance;

    public MenuManager(JavaPlugin instance) {
        this.instance = instance;
        Bukkit.getPluginManager().registerEvents(this, instance);
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
        menus.forEach((menuId, menu) -> {
            menu.getPages().forEach((integer, menuPage) -> {
                menuPage.getItems().forEach((slot, item) -> {
                    if (event.getSlot() != slot) {
                        return;
                    }
                    boolean menuItemHasKey = itemStackHasKey(item.getItemStack(), "vc-menu-acceptclick");
                    if (event.getCurrentItem() == null) {
                        return;
                    }
                    boolean clickedItemHasKey = itemStackHasKey(event.getCurrentItem(), "vc-menu-acceptclick");
                    if (menuItemHasKey && clickedItemHasKey) {
                        item.getClickAction().accept(event);
                    }
                });
            });
        });
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
