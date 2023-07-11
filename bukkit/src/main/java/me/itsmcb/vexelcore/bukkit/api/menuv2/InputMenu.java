package me.itsmcb.vexelcore.bukkit.api.menuv2;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.function.Consumer;

public class InputMenu extends MenuV2 {

    private Consumer<String> string = null;

    public InputMenu(String title, MenuV2Item item, Player player) {
        super(title, InventoryType.ANVIL);
        addStaticItem(item.slot(0));
    }

    public Consumer<String> getString() {
        return string;
    }

    public InputMenu string(Consumer<String> string) {
        this.string = string;
        return this;
    }

}
