package me.itsmcb.vexelcore.bukkit.api.menuv2;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MenuHolder implements InventoryHolder {

    private UUID uuid;

    public MenuHolder(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
