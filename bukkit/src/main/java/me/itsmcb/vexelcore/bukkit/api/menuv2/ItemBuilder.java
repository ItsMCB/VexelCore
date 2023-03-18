package me.itsmcb.vexelcore.bukkit.api.menuv2;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {

    Component name;
    Material material;

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemBuilder name(Component name) {
        this.name = name;
        return this;
    }

    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.displayName(name);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
