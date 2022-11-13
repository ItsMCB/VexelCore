package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class MenuItem {

    ItemStack itemStack;
    Consumer<InventoryClickEvent> clickAction;
    String trackingId;

    public MenuItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public MenuItem(ItemStack itemStack, String trackingId, JavaPlugin instance) {
        this.itemStack = itemStack;
        this.trackingId = trackingId;
        setTrackingId(trackingId, instance);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setClickAction(Consumer<InventoryClickEvent> clickAction) {
        this.clickAction = clickAction;
    }

    public Consumer<InventoryClickEvent> getClickAction() {
        return clickAction;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId, JavaPlugin instance) {
        this.trackingId = trackingId;
        NamespacedKey key = new NamespacedKey(instance, "menu-tracking-id");
        this.itemStack.getItemMeta().getPersistentDataContainer().set(key, PersistentDataType.STRING, trackingId);
    }
}
