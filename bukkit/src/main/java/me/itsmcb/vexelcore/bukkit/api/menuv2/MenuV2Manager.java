package me.itsmcb.vexelcore.bukkit.api.menuv2;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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

    /*
    @EventHandler
    public void InventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) {
            return;
        }
        if (holder instanceof MenuHolder menuHolder) {
            try {
                menus.remove(getMenuFromUUID(menuHolder.getUUID()));
            } catch (NoSuchElementException e) {
                // Do nothing
            }
        }
    }

     */

    private Optional<MenuV2> getOptionalMenu(InventoryHolder holder) {
        if (holder == null) {
            return Optional.empty();
        }
        if (!(holder instanceof MenuHolder)) {
            return Optional.empty();
        }

        MenuHolder menuHolder = (MenuHolder) holder;
        Optional<MenuV2> optionalMenuV2 = getMenuFromHolder(menuHolder);
        if (optionalMenuV2.isEmpty()) {
            return Optional.empty();
        }
        MenuV2 menu;
        try {
            menu = getMenuFromUUID(menuHolder.getUUID());
        } catch (NoSuchElementException e) {
            // Not a valid menu
            return Optional.empty();
        }
        return Optional.of(menu);
    }

    @EventHandler
    public void InventoryClick(InventoryClickEvent event) {
        Optional<MenuV2> optionalMenu = getOptionalMenu(event.getInventory().getHolder());
        if (optionalMenu.isEmpty()) {
            return;
        }
        MenuV2 menu = optionalMenu.get();
        ItemStack currentItem = event.getCurrentItem();

        if (currentItem == null) {
            return;
        }

        UUID itemUUID = getMenuItemUUID(currentItem);
        Optional<MenuV2Item> optional = menu.getItems().stream().filter(item -> item.getUUID().equals(itemUUID)).findFirst();
        if (optional.isEmpty()) {
            // Check if it's a static item
            optional = menu.getStaticItems().stream().filter(item -> item.getUUID().equals(itemUUID)).findFirst();
            if (optional.isEmpty()) {
                // Did not click a valid item
                return;
            }
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
        // If still viewing menu because a new one didn't open, check closing click
        if (menu.shouldClickCloseMenu() && !event.getInventory().getViewers().isEmpty()) {
            if (menu instanceof PaginatedMenu && item instanceof MenuButton) {
                return;
            }
            player.closeInventory();
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

    private Optional<MenuV2> getMenuFromHolder(MenuHolder menuHolder) {
        return menus.stream().filter(menu -> menu.getMenuHolder().getUUID() == menuHolder.getUUID()).findFirst();
    }

    public void open(MenuV2 menu, Player player) {
        System.out.println("Setting manager");
        menu.setManager(this);
        menus.add(menu);
        player.openInventory(menu.generate(player));
    }

    public void open(MenuV2 menu, Player player, MenuV2 previousMenu) {
        System.out.println("Setting pm");
        menu.setPreviousMenu(previousMenu);
        open(menu,player);
    }

}
