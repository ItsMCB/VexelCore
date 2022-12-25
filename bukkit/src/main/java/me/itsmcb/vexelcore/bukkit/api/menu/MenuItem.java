package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.function.Consumer;

public class MenuItem {

    private ItemStack itemStack;
    private Consumer<InventoryClickEvent> rightClickAction = null;
    private Consumer<InventoryClickEvent> leftClickAction = null;
    private UUID id = null;

    public MenuItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setClickActions(Consumer<InventoryClickEvent> clickAction) {
        setLeftClick(clickAction);
        setRightClick(clickAction);
    }

    public void setLeftClick(Consumer<InventoryClickEvent> clickAction) {
        this.leftClickAction = clickAction;
    }

    public void setRightClick(Consumer<InventoryClickEvent> clickAction) {
        this.rightClickAction = clickAction;
    }

    public Consumer<InventoryClickEvent> getRightClickAction() {
        return rightClickAction;
    }

    public Consumer<InventoryClickEvent> getLeftClickAction() {
        return leftClickAction;
    }

    public void setKey(NamespacedKey key, String data) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
        this.itemStack.setItemMeta(itemMeta);
    }
}
