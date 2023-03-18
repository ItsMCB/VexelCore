package me.itsmcb.vexelcore.bukkit.api.menuv2;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class MenuV2Item {

    private ItemBuilder itemBuilder;

    private boolean movable = false;

    private Consumer<InventoryClickEvent> rightClickAction = null;
    private Consumer<InventoryClickEvent> leftClickAction = null;

    public MenuV2Item(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
    }

    public MenuV2Item movable(boolean movable) {
        this.movable = movable;
        return this;
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

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    public Consumer<InventoryClickEvent> getRightClickAction() {
        return rightClickAction;
    }

    public Consumer<InventoryClickEvent> getLeftClickAction() {
        return leftClickAction;
    }
}
