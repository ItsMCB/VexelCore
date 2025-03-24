package me.itsmcb.vexelcore.bukkit.api.menuv2;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;

public class PaginatedMenu extends MenuV2 {

    public PaginatedMenu(String title, int size, Player player) {
        super(title, InventoryType.CHEST, size);
        // Beginning
        addStaticItem(new MenuV2Item(Material.BLACK_STAINED_GLASS_PANE).name("&7").slot(size-9));
        addStaticItem(new MenuV2Item(Material.BLACK_STAINED_GLASS_PANE).name("&7").slot(size-8));
        // Back Button
        String arrowLeft = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19";

        addStaticItem(new LegacyPaginatedMenuButton(arrowLeft)
                .name("&r&d&lPrevious Page")
                .slot(size-7)
                .leftClickAction(event -> {
                    int newIndex = firstItemIndexIfPageAdded(-1);
                    if (canChangePage(newIndex)) {
                        setPage(getPage()-1);
                        updatePage(player,newIndex);
                    } else {
                        new BukkitMsgBuilder("&7You've reached the beginning!").send(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, (float) 1, 0);
                    }
        }));
        // Middle
        addStaticItem(new MenuV2Item(Material.BLACK_STAINED_GLASS_PANE).name("&7").slot(size-6));
        addStaticItem(new MenuV2Item(Material.BLACK_STAINED_GLASS_PANE).name("&7").slot(size-4));
        // Forward Button
        String arrowRight = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0";
        addStaticItem(new LegacyPaginatedMenuButton(arrowRight)
                .name("&r&d&lNext Page")
                .slot(size-3)
                .leftClickAction(event -> {
                    int newIndex = firstItemIndexIfPageAdded(1);
                    if (canChangePage(newIndex)) {
                        setPage(getPage()+1);
                        updatePage(player,newIndex);
                    } else {
                        new BukkitMsgBuilder("&7You've reached the end!").send(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, (float) 1, 0);
                    }
        }));
        // End
        addStaticItem(new MenuV2Item(Material.BLACK_STAINED_GLASS_PANE).name("&7").slot(size-2));
        addStaticItem(new MenuV2Item(Material.BLACK_STAINED_GLASS_PANE).name("&7").slot(size-1));
    }

    private int firstItemIndexIfPageAdded(int pagesToAdd) {
        int newPage = (getPage()-1)+pagesToAdd;
        int usableSpace = getSize()-staticItemsSize();
        return newPage*usableSpace;
    }

    private void updatePage(Player player, int firstItemIndex) {
        // Limit amount to what's needed to update to prevent lag
        int amountOfItems = getItems().size();
        int end = (firstItemIndex+(getSize()-getStaticItems().size()));
        int temp = end;
        if (amountOfItems < end) {
            temp = amountOfItems;
        }
        setCurrentItems(new ArrayList<>(getItems().subList(firstItemIndex, temp)), player);
    }

    private boolean canChangePage(int start) {
        if (start >= getItems().size() || start < 0) {
            return false;
        }
        return true;
    }

    @Override
    public PaginatedMenu setPreviousMenu(MenuV2 previousMenu) {
        super.setPreviousMenu(previousMenu);
        return this;
    }

    @Override
    public PaginatedMenu setManager(MenuV2Manager manager) {
        super.setManager(manager);
        return this;
    }

    @Override
    public PaginatedMenu clickCloseMenu(boolean bool) {
        super.clickCloseMenu(bool);
        return this;
    }

    @Override
    public void setInventoryItems(Player player) {
        if (super.getPreviousMenu() != null) {
            // Show back arrow
            String arrowBack = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU5YWU3YTRiZTY1ZmNiYWVlNjUxODEzODlhMmY3ZDQ3ZTJlMzI2ZGI1OWVhM2ViNzg5YTkyYzg1ZWE0NiJ9fX0";
            addStaticItem(new LegacyPaginatedMenuButton(arrowBack)
                    .name("&r&d&lBack")
                    .slot(super.getSize()-5)
                    .leftClickAction(event -> {
                        getManager().open(super.getPreviousMenu(),player,this);
                    }));
        } else {
            addStaticItem(new MenuV2Item(Material.BLACK_STAINED_GLASS_PANE).name("&7").slot(super.getSize()-5));
        }
        super.setInventoryItems(player);
    }
}
