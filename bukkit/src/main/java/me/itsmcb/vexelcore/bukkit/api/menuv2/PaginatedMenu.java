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
        String arrowLeft = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19";

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
        String arrowRight = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0";
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
        // Limit amount to what's needed to update to prevent lag
        int end = getItems().size();
        int temp = firstItemIndex+getSize();
        if (firstItemIndex+getSize() < end) {
            end = temp;
        }
        setCurrentItems(new ArrayList<>(getItems().subList(firstItemIndex, end)), player);
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
