package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.VexelCoreBukkitAPI;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Handles {@link Menu} events.
 * <p>
 * Please access with {@link VexelCoreBukkitAPI#getMenuManager()}
 */
public class MenuManager implements Listener {

    /**
     * The {@link NamespacedKey} applied to the persistent data container of {@link MenuButton}s for event handling.
     */
    public static NamespacedKey menuSystemIdKey = new NamespacedKey("vc-menu-system","vc-menu-item-id");
    private ArrayList<Menu> registeredMenus = new ArrayList<>();
    private HashMap<Menu,Menu> previousMenusSet = new HashMap<>();
    private JavaPlugin instance;

    public MenuManager(@NotNull JavaPlugin instance) {
        this.instance = instance;
    }

    /**
     * Registers a menu to enable handling.
     *
     * @param menu The menu to register.
     * @return The registered {@link Menu} object.
     */
    public Menu register(@NotNull Menu menu) {
        registeredMenus.add(menu);
        return menu;
    }

    /**
     * Opens the menu for the player. Will register the menu first if not already registered.
     *
     * @param menu The menu to open (and possibly register).
     * @param player The player to open the menu for.
     * @return The {@link Menu} object (builder pattern).
     */
    public Menu open(@NotNull Menu menu, @NotNull Player player) {
        if (!registeredMenus.contains(menu)) {
            register(menu);
        }
        menu.open(player);
        return menu;
    }

    /**
     * Retrieves the unique identifier of a {@link ItemStack}.
     * The UUID is stored in the item's persistent data container using the {@link #menuSystemIdKey}.
     *
     * @param itemStack The {@link ItemStack} to retrieve the menu item UUID from.
     * @return The {@link UUID} of the menu item, or {@code null} if the item is not a menu item or does not have a UUID.
     */
    private UUID getMenuItemUUID(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        if (!container.has(menuSystemIdKey)) {
            return null;
        }
        String foundUUID = container.get(menuSystemIdKey, PersistentDataType.STRING);
        if (foundUUID == null) {
            return null;
        }
        return UUID.fromString(foundUUID);
    }

    /**
     * Retrieves a list of all template buttons from all registered menus.
     * Template buttons are typically static elements like navigation buttons.
     *
     * @return A {@link List} of {@link MenuButton}s from all registered menus.
     */
    private List<MenuButton> getAllTemplateButtons() {
        ArrayList<MenuButton> buttons = new ArrayList<>();
        registeredMenus.forEach(menu -> buttons.addAll(menu.getTemplatePositionedButtons().values()));
        return buttons;
    }

    /**
     * Retrieves a list of all menu buttons (template, positioned, and unpositioned) from all registered menus.
     *
     * @return A {@link List} of all {@link MenuButton}s across all registered menus.
     */
    private List<MenuButton> getAllMenuButtons() {
        ArrayList<MenuButton> buttons = new ArrayList<>();
        registeredMenus.forEach(menu -> {
            buttons.addAll(menu.getTemplatePositionedButtons().values());
            buttons.addAll(menu.getPositionedButtons().values());
            buttons.addAll(menu.getUnpositionedButtons());
        });
        return buttons;
    }

    /**
     * Handles the click interactions of registered {@link Menu}s.
     *
     * @param event The {@link InventoryClickEvent} to handle.
     */
    @EventHandler
    public void InventoryClick(@NotNull InventoryClickEvent event) {
        // Handle non-menu click
        if (event.getClickedInventory() == null) {
            return;
        }
        if (!(event.getClickedInventory().getHolder() instanceof Menu menu)) {
            return;
        }
        ItemStack cursorItem = event.getCurrentItem();
        if (cursorItem == null) {
            return;
        }
        // Determine if it's a menu item
        UUID itemUUID = getMenuItemUUID(cursorItem);
        Optional<MenuButton> optional = getAllMenuButtons().stream().filter(m -> m.getUUID().equals(itemUUID)).findFirst();
        if (optional.isEmpty()) {
            return;
        }
        MenuButton button = optional.get();
        // Handle movement
        if (!button.isMoveable()) {
            event.setCancelled(true);
        }
        // Close menu if necessary before handling click (which may include opening a new menu which should not be interfered with)
        Player player = (Player) event.getWhoClicked();
        boolean isTemplateButton = getAllTemplateButtons().contains(optional.get());
        // Don't close if user clicked on a template element (ex. forward/back button)
        if (menu.isClickToClose() && !(isTemplateButton)) {
            player.closeInventory();
        }
        // Update player inventory to prevent client-side glitches
        player.updateInventory();
        // Handle clicks
        if (event.getClick().isRightClick() && button.getRightClick() != null) {
            button.getRightClick().accept(event);
        }
        if (event.getClick().isLeftClick() && button.getLeftClick() != null) {
            button.getLeftClick().accept(event);
        }
        // Moved via number key press (to hot bar)
        if (event.getClick().isKeyboardClick() && button.getRightClick() != null) {
            button.getRightClick().accept(event);
        }
    }

    public void setPreviousMenu(@NotNull Menu menu, @NotNull Menu previous) {
        previousMenusSet.put(menu,previous);
    }

    public Menu removePreviousMenuFromSelf(@NotNull Menu menu) {
        return previousMenusSet.remove(menu); // Returns previous menu
    }

    public Menu getPreviousMenuOrNull(@NotNull Menu menu) {
        return previousMenusSet.get(menu);
    }

    public boolean hasPreviousMenu(@NotNull Menu menu) {
        return previousMenusSet.containsKey(menu);
    }

    public boolean isPreviousMenuForAnyMenu(@NotNull Menu menu) {
        return previousMenusSet.containsValue(menu);
    }

    public boolean openPrevious(@NotNull Menu menu, @NotNull Player player) {
        Menu prev = getPreviousMenuOrNull(menu);
        if (prev == null) {
            return false;
        }
        prev.open(player);
        return true;
    }

    /**
     * Handles the lifecycle of {@link Menu}s.
     * <p>
     * If the menu is not persistent, it will be unregistered and its runnable refresher will be cancelled.
     *
     * @param event The {@link InventoryClickEvent} to handle.
     */
    @EventHandler
    public void InventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu menu)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                // If the closed menu is assigned as a previous menu to any other menu, don't destroy
                if (isPreviousMenuForAnyMenu(menu)) {
                    // This menu will be destroyed later when the menu that has it set as previous is destroyed
                    return;
                }
                // Not a previous menu
                // Keep if persistent, or remove
                if (menu.isPersistent()) {
                    return;
                }
                menu.unload();
                registeredMenus.remove(menu);
                // Clean up previous menus
                Menu prev = removePreviousMenuFromSelf(menu);
                if (prev == null) {
                    return;
                }
                if (prev.isPersistent()) {
                    return;
                }
                // Check if set as previous menu for any other
                if (isPreviousMenuForAnyMenu(prev)) {
                    return;
                }
                // Check if previous menu is now the open menu
                if (!prev.getInventory().getViewers().isEmpty()) {
                    return;
                }
                // Previous menu is now the open menu
                prev.unload();
                registeredMenus.remove(prev);
            }
        }.runTaskLater(instance,100); // Wait 5 seconds before unloading to give time for the menu to be assigned as a previous menu between transitions
    }
}
