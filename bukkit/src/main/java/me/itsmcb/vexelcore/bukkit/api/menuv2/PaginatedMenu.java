package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class PaginatedMenu extends MenuV2 {

    public PaginatedMenu(String title, int size, Player player) {
        super(title, InventoryType.CHEST, size);
        // Beginning
        addStaticItem(new MenuV2Item(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&7")).slot(27));
        addStaticItem(new MenuV2Item(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&7")).slot(28));
        // Back Button
        String arrowLeft = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE4NWM5N2RiYjgzNTNkZTY1MjY5OGQyNGI2NDMyN2I3OTNhM2YzMmE5OGJlNjdiNzE5ZmJlZGFiMzVlIn19fQ";

        addStaticItem(new MenuV2Item(new SkullBuilder(arrowLeft)
                .name("&r&d&lLast Page"))
                .slot(29)
                .leftClickAction(event -> {
                    int newIndex = firstItemIndexIfPageAdded(-1);
                    if (canChangePage(newIndex)) {
                        setPage(getPage()-1);
                        updatePage(player,newIndex);
                    } else {
                        new BukkitMsgBuilder("&cCan't go backward").send(player);
                    }
        }));
        // Middle
        addStaticItem(new MenuV2Item(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&7")).slot(30));
        addStaticItem(new MenuV2Item(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&7")).slot(31));
        addStaticItem(new MenuV2Item(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&7")).slot(32));
        // Forward Button
        String arrowRight = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFjMGVkZWRkNzExNWZjMWIyM2Q1MWNlOTY2MzU4YjI3MTk1ZGFmMjZlYmI2ZTQ1YTY2YzM0YzY5YzM0MDkxIn19fQ";
        addStaticItem(new MenuV2Item(new SkullBuilder(arrowRight)
                .name("&r&d&lNext Page"))
                .slot(33)
                .leftClickAction(event -> {
                    int newIndex = firstItemIndexIfPageAdded(1);
                    if (canChangePage(newIndex)) {
                        setPage(getPage()+1);
                        updatePage(player,newIndex);
                    } else {
                        new BukkitMsgBuilder("&cCan't go forward").send(player);
                    }
        }));
        // End
        addStaticItem(new MenuV2Item(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&7")).slot(34));
        addStaticItem(new MenuV2Item(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&7")).slot(35));
    }

    private int firstItemIndexIfPageAdded(int pagesToAdd) {
        int newPage = (getPage()-1)+pagesToAdd;
        int usableSpace = getSize()-staticItemsSize();
        return newPage*usableSpace;
    }

    private void updatePage(Player player, int firstItemIndex) {
        setCurrentItems(new ArrayList<>(getItems().subList(firstItemIndex, getItems().size())), player);
    }

    private boolean canChangePage(int start) {
        if (start > getItems().size() || start < 0) {
            return false;
        }
        return true;
    }

    @Override
    public Inventory generateInventory() {
        MenuHolder menuHolder = new MenuHolder(super.getUUID());
        return Bukkit.createInventory(menuHolder, getSize(), new BukkitMsgBuilder(getTitle()).get());
    }

}
