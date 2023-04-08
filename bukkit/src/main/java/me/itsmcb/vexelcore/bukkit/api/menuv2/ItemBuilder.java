package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private Component name;
    private Material material;

    private List<Component> lore = new ArrayList<>();

    private ArrayList<MenuV2ItemData> data = new ArrayList<>();

    private int amount = 1;

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

    public ItemBuilder name(String name) {
        // Remove default italic decoration
        name(new BukkitMsgBuilder(name).get().decoration(TextDecoration.ITALIC, false));
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        ArrayList<Component> editedLore = new ArrayList<>();
        lore.forEach(element -> {
            editedLore.add(element.color(TextColor.color(170,170,170)).decoration(TextDecoration.ITALIC, false));
        });
        this.lore = editedLore;
        return this;
    }

    public ItemBuilder lore(Component lore) {
        this.lore = List.of(lore);
        return this;
    }

    public ItemBuilder lore(String lore) {
        this.lore = List.of(new BukkitMsgBuilder(lore).get());
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder addData(MenuV2ItemData data) {
        this.data.add(data);
        return this;
    }

    public ItemStack getItemStack() {
        ItemStack itemStack = getCleanItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        data.forEach(data -> {
            if (data.getKey().getNamespace().equals(MenuV2Manager.menuSystemIdKey.getNamespace())) {
                itemMeta.getPersistentDataContainer().set(data.getKey(), PersistentDataType.STRING, data.getString());
            }
        });
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getCleanItemStack() {
        ItemStack itemStack = new ItemStack(material);
        itemStack.setAmount(amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.displayName(name);
        }
        itemMeta.lore(lore);
        data.forEach(data -> {
            if (data.getType().equals(PersistentDataType.STRING) || !(data.getKey().equals(MenuV2Manager.menuSystemIdKey))) {
                itemMeta.getPersistentDataContainer().set(data.getKey(), PersistentDataType.STRING, data.getString());
            }
        });
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
