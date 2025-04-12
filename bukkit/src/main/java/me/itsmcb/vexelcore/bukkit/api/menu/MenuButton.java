package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.api.cache.CachedPlayerV2;
import me.itsmcb.vexelcore.bukkit.api.cache.PlayerSkinData;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.bukkit.api.utils.SkullBuilderUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuButton {

    // Tracking
    private UUID uuid = UUID.randomUUID();

    // Item properties
    private Component name;
    private ArrayList<TextComponent> lore = new ArrayList<>();
    private ArrayList<ItemData> persistantItemData = new ArrayList<>();

    // Item click events
    private boolean moveable = false;
    private Consumer<InventoryClickEvent> rightClick = null;
    private Consumer<InventoryClickEvent> leftClick = null;

    // Malleable item stack
    private ItemStack itemStack;

    public MenuButton(@NotNull Material material) {
        this.itemStack = new ItemStack(material);
    }

    public MenuButton(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public MenuButton(@NotNull CachedPlayerV2 cachedPlayer) {
        this.itemStack = new SkullBuilderUtil(cachedPlayer.getPlayerSkinData().getTexture(),cachedPlayer.getPlayerSkinData().getSignature()).get();
    }

    public MenuButton(PlayerSkinData playerSkinData) {
        this.itemStack = new SkullBuilderUtil(playerSkinData).get();
    }

    public MenuButton(@NotNull String texture) {
        this.itemStack = new SkullBuilderUtil(texture).get();
    }

    public MenuButton name(String name) {
        name(new BukkitMsgBuilder(name).get());
        return this;
    }

    public MenuButton name(Component name) {
        this.name = name;
        return this;
    }

    public MenuButton addLore(String... lore) {
        List<TextComponent> components = new ArrayList<>();
        for (String s : lore) {
            components.add(new BukkitMsgBuilder(s).get());
        }
        this.lore.addAll(components);
        return this;
    }

    public MenuButton setLore(ArrayList<TextComponent> lore) {
        this.lore = lore;
        return this;
    }

    public MenuButton setLore(TextComponent... textComponents) {
        this.setLore(new ArrayList<>(Arrays.asList(textComponents)));
        return this;
    }

    public ArrayList<ItemData> getPersistantItemData() {
        return persistantItemData;
    }

    public MenuButton addPersistantItemData(ItemData itemData) {
        this.getPersistantItemData().add(itemData);
        return this;
    }

    public MenuButton setPersistantItemData(ArrayList<ItemData> persistantItemData) {
        this.persistantItemData = persistantItemData;
        return this;
    }

    public MenuButton rightClick(Consumer<InventoryClickEvent> rightClick) {
        this.rightClick = rightClick;
        return this;
    }

    public Consumer<InventoryClickEvent> getRightClick() {
        return rightClick;
    }

    public MenuButton leftClick(Consumer<InventoryClickEvent> leftClick) {
        this.leftClick = leftClick;
        return this;
    }

    public MenuButton click(Consumer<InventoryClickEvent> click) {
        this.rightClick = click;
        this.leftClick = click;
        return this;
    }

    public Consumer<InventoryClickEvent> getLeftClick() {
        return leftClick;
    }

    public Material getType() {
        return itemStack.getType();
    }

    /**
     * Updates ItemStack with the latest settings.
     * <p>
     * Note: This is where the menu system key is applied. This is necessary for the handling done in {@link MenuManager}.
     */
    public MenuButton refresh() {
        ItemMeta meta = itemStack.getItemMeta();
        // Name
        if (name != null) {
            meta.displayName(Component.text().decoration(TextDecoration.ITALIC, false).append(name).build());
        }
        // Lore
        meta.lore(lore.stream().map(l -> l.decoration(TextDecoration.ITALIC, false)).toList());
        // Tracking ID
        meta.getPersistentDataContainer().set(MenuManager.menuSystemIdKey, PersistentDataType.STRING, uuid+"");
        // Item data
        if (!getPersistantItemData().isEmpty()) {
            getPersistantItemData().forEach(itemData -> {
                meta.getPersistentDataContainer().set(itemData.getNamespacedKey(),itemData.getPersistentDataType(),itemData.getData());
            });
        }
        // Apply
        itemStack.setItemMeta(meta);
        return this;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public boolean isMoveable() {
        return moveable;
    }

    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the current state of the ItemStack
     */
    public ItemStack get() {
        return itemStack;
    }

    public ItemStack getCleanItemStack() {
        ItemStack cleanIS = itemStack;
        ItemMeta itemMeta = cleanIS.getItemMeta();
        itemMeta.getPersistentDataContainer().remove(MenuManager.menuSystemIdKey);
        cleanIS.setItemMeta(itemMeta);
        return cleanIS;
    }

}
