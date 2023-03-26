package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import net.kyori.adventure.text.Component;
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

    private ItemStack itemStack;

    private ArrayList<MenuV2ItemData> data = new ArrayList<>();

    public ItemBuilder(Material material) {
        this.material = material;
        this.itemStack = new ItemStack(material);
    }

    public Material getMaterial() {
        return material;
    }

    public ItemBuilder name(Component name) {
        this.name = name;
        return this;
    }

    public ItemBuilder name(String name) {
        this.name = new BukkitMsgBuilder("&r"+name).get();
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        this.lore = lore;
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
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder addData(MenuV2ItemData data) {
        this.data.add(data);
        return this;
    }

    public ItemStack getItemStack() {
        ItemMeta itemMeta = getCleanItemStack().getItemMeta();
        data.forEach(data -> {
            if (data.getType().equals(PersistentDataType.STRING)) {
                itemMeta.getPersistentDataContainer().set(data.getKey(), PersistentDataType.STRING, data.getString());
            }
        });
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getCleanItemStack() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.displayName(name);
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
