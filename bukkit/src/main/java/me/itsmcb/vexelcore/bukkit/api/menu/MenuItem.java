package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

public class MenuItem {

    ItemStack itemStack;
    Consumer<InventoryClickEvent> clickAction;

    public MenuItem(ItemStack itemStack) {
        this.itemStack = itemStack;
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

    public void setKey(NamespacedKey key, String data) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
        for (NamespacedKey namespacedKey : itemStack.getItemMeta().getPersistentDataContainer().getKeys()) {
            System.out.println("Debug after key added: " + namespacedKey.getNamespace() + " | " + namespacedKey.value());
        }
        this.itemStack.setItemMeta(itemMeta);
    }
}
