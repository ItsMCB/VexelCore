package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuV2Item extends ItemStack {

    // MENU ITEM STUFF

    private boolean movable = false;
    private boolean update = true;

    private int slot = -1;

    private UUID uuid = UUID.randomUUID();

    private Consumer<InventoryClickEvent> rightClickAction = null;
    private Consumer<InventoryClickEvent> leftClickAction = null;

    // TODO REMOVE THIS AND FIX ON ALL PLUGINS!!!!!!!
    public MenuV2Item(ItemBuilder itemBuilder) {
        this(itemBuilder.getMaterial());
        //this.itemBuilder = itemBuilder;
        //itemBuilder.addData(new MenuV2ItemData(MenuV2Manager.menuSystemIdKey, uuid.toString()));
    }

    public MenuV2Item(Material material) {
        super(material);
        this.slot(-1);
        addData(new MenuV2ItemData(MenuV2Manager.menuSystemIdKey, uuid.toString()));
    }

    public MenuV2Item createNewCopy() {
        this.uuid = UUID.randomUUID();
        replaceData(new MenuV2ItemData(MenuV2Manager.menuSystemIdKey, uuid.toString()));
        return this;
    }

    public MenuV2Item shouldUpdate(boolean answer) {
        this.update = answer;
        return this;
    }

    public boolean shouldUpdate() {
        return update;
    }

    public MenuV2Item movable(boolean movable) {
        this.movable = movable;
        return this;
    }

    public MenuV2Item slot(int slot) {
        this.slot = slot;
        return this;
    }

    public int getSlot() {
        return slot;
    }

    public MenuV2Item rightClickAction(Consumer<InventoryClickEvent> rightClickAction) {
        this.rightClickAction = rightClickAction;
        return this;
    }

    public MenuV2Item leftClickAction(Consumer<InventoryClickEvent> leftClickAction) {
        this.leftClickAction = leftClickAction;
        return this;
    }

    public boolean isMovable() {
        return this.movable;
    }

    public Consumer<InventoryClickEvent> getRightClickAction() {
        return rightClickAction;
    }

    public Consumer<InventoryClickEvent> getLeftClickAction() {
        return leftClickAction;
    }

    public UUID getUUID() {
        return uuid;
    }

    // ITEM BUILDER STUFF
    private Component name;

    private ArrayList<MenuV2ItemData> data = new ArrayList<>();

    private int amount = 1;

    public MenuV2Item name(TextComponent name) {
        // Remove default italic decoration
        ItemMeta itemMeta = getItemMeta();
        itemMeta.displayName(Component.text().decoration(TextDecoration.ITALIC, false).append(name).build());
        setItemMeta(itemMeta);
        return this;
    }

    public MenuV2Item name(String name) {
        name(new BukkitMsgBuilder(name).get());
        return this;
    }

    public MenuV2Item lore(TextComponent lore) {
        ArrayList<Component> components = this.getItemMeta().hasLore() ? new ArrayList<>(this.getItemMeta().lore()) : new ArrayList<>();
        components.add(lore);
        ItemMeta itemMeta = getItemMeta();
        // TODO figure out why it doesn't remove the italics
        itemMeta.lore(components.stream().map(c -> c.decoration(TextDecoration.ITALIC, false)).toList());
        setItemMeta(itemMeta);
        return this;
    }

    public MenuV2Item lore(String lore) {
        lore(List.of(new BukkitMsgBuilder(lore).get()));
        return this;
    }

    public MenuV2Item lore(String... lore) {
        List<Component> components = new ArrayList<>();
        for (String s : lore) {
            components.add(new BukkitMsgBuilder(s).get());
        }
        lore(components);
        return this;
    }

    public MenuV2Item lore (TextComponent... lore) {
        lore(Arrays.stream(lore).toList());
        return this;
    }

    public MenuV2Item amount(int amount) {
        this.amount(amount);
        return this;
    }

    public MenuV2Item addData(MenuV2ItemData data) {
        ItemMeta itemMeta = getItemMeta();
        if (data.getType().equals(PersistentDataType.STRING)) {
            itemMeta.getPersistentDataContainer().set(data.getKey(), PersistentDataType.STRING, data.getString());
        }
        setItemMeta(itemMeta);
        return this;
    }

    public MenuV2Item removeData(MenuV2ItemData data) {
        ItemMeta itemMeta = getItemMeta();
        if (data.getType().equals(PersistentDataType.STRING)) {
            itemMeta.getPersistentDataContainer().remove(data.getKey());
        }
        setItemMeta(itemMeta);
        return this;
    }

    public MenuV2Item replaceData(MenuV2ItemData data) {
        ItemMeta itemMeta = getItemMeta();
        if (data.getType().equals(PersistentDataType.STRING)) {
            itemMeta.getPersistentDataContainer().remove(data.getKey());
            itemMeta.getPersistentDataContainer().set(data.getKey(), PersistentDataType.STRING, data.getString());
        }
        setItemMeta(itemMeta);
        return this;
    }

    public ItemStack getCleanItemStack() {
        ItemStack itemStack = this.clone();
        itemStack.getItemMeta().getPersistentDataContainer().remove(MenuV2Manager.menuSystemIdKey);
        return itemStack;
    }

    public MenuV2Item update() {
        return this;
    }

    public MenuV2Item updateAndShouldAgain(Boolean bool) {
        this.update = bool;
        update();
        return this;
    }


}
