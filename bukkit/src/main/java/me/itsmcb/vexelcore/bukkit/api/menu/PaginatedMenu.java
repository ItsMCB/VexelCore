package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.VexelCoreBukkitAPI;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.common.api.HeadTexture;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

/**
 * Custom chest menu with automatic pagination controlled by navigation. {@link MenuButton}s are automatically refreshed and arranged.
 * <p>
 * Note: The bottom row (9 slots) are reserved for navigation. Thus, don't use {@link MenuRowSize#ONE} as other {@link MenuButton} won't have enough space to be displayed.
 */
public class PaginatedMenu extends Menu {

    private int page = 1;

    public PaginatedMenu(MenuRowSize size, String title) {
        super(size, title);
        addPaginatedButtons();
    }

    /**
     * Add navigation {@link MenuButton}s to template.
     */
    private void addPaginatedButtons() {
        // Left
        setTemplateButton(getSize().getSize()-9,new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7"));
        setTemplateButton(getSize().getSize()-8,new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7"));
        // Previous Arrow
        setTemplateButton(getSize().getSize()-7,new MenuButton(HeadTexture.GRAY_ARROW_LEFT.getTexture())
                .name("&r&d&lPrevious Page")
                .click(e -> {
                    Player player = (Player) e.getWhoClicked();
                    if (!pageGoBack()) {
                        new BukkitMsgBuilder("&7You've reached the beginning!").send(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, (float) 1, 0);
                    }
                })
        );
        // Middle
        setTemplateButton(getSize().getSize()-6,new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7"));
        setTemplateButton(getSize().getSize()-5,new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7"));
        setTemplateButton(getSize().getSize()-4,new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7"));
        // Forward Arrow
        setTemplateButton(getSize().getSize()-3, new MenuButton(HeadTexture.GRAY_ARROW_RIGHHT.getTexture())
                .name("&r&d&lNext Page")
                .click(e -> {
                    Player player = (Player) e.getWhoClicked();
                    if (!pageGoForward()) {
                        new BukkitMsgBuilder("&7You've reached the end!").send(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, (float) 1, 0);
                    }
                })
        );
        // Right
        setTemplateButton(getSize().getSize()-2,new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7"));
        setTemplateButton(getSize().getSize()-1,new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7"));
    }

    public int getPage() {
        return page;
    }

    public boolean pageGoBack() {
        // Check if the current page is already at the first page
        if (page <= 1) {
            page = 1;
            return false;
        }
        // Go to the previous page
        page--;
        refresh();
        return true;
    }

    public boolean pageGoForward() {
        int menuContentSize = getPositionedButtons().size() + getUnpositionedButtons().size();
        int availableInventorySpace = getSize().getSize() - getTemplatePositionedButtons().size();
        int totalPages = (int) Math.ceil((double) menuContentSize / availableInventorySpace);
        // Check if the current page is already the last page
        if (page >= totalPages) {
            return false;
        }
        // Go to the next page
        page++;
        refresh();
        return true;
    }

    @Override
    public void refresh() {
        inventory.clear();
        final int inventorySize = getSize().getSize();
        final int templateButtonSize = getTemplatePositionedButtons().size();
        final int availableButtonSlots = inventorySize - templateButtonSize;

        applyTemplateButtons();
        int filledSlots = applyPositionedButtons(availableButtonSlots);
        applyUnpositionedButtons(inventorySize, filledSlots);
    }

    /**
     * Applies template buttons to the inventory.
     */
    private void applyTemplateButtons() {
        if (VexelCoreBukkitAPI.getMenuManager().hasPreviousMenu(this)) {
            int middle = getSize().getSize()-5;
            if (!getTemplatePositionedButtons().get(middle).getType().equals(Material.PLAYER_HEAD)) {
                setTemplateButton(middle, new MenuButton(HeadTexture.GRAY_ARROW_DOWN.getTexture()).name("&r&d&lBack").click(e -> {
                    VexelCoreBukkitAPI.getMenuManager().openPrevious(this, ((Player) e.getWhoClicked()).getPlayer());
                }));
            }
        }
        getTemplatePositionedButtons().forEach((position, button) ->
                inventory.setItem(position, button.refresh().get())
        );
    }

    /**
     * Applies positioned buttons for the current page to the inventory.
     *
     * @param availableContentSpace Number of available slots for content
     * @return Total number of filled slots after applying positioned buttons
     */
    private int applyPositionedButtons(int availableContentSpace) {
        int startIndexPositioned = (page - 1) * availableContentSpace;
        int endIndexPositioned = startIndexPositioned + availableContentSpace;
        getPositionedButtons().entrySet().stream()
                .filter(entry -> entry.getKey() >= startIndexPositioned && entry.getKey() < endIndexPositioned)
                .forEach(e -> inventory.setItem(e.getKey() - startIndexPositioned, e.getValue().refresh().get()));
        return countFilledSlots();
    }

    /**
     * Applies unpositioned buttons to remaining inventory slots.
     *
     * @param inventorySize Total inventory size
     * @param filledSlots Number of slots already filled
     */
    private void applyUnpositionedButtons(int inventorySize, int filledSlots) {
        final int availableContentSpace = inventorySize - getTemplatePositionedButtons().size();
        final int spaceRemaining = inventorySize - filledSlots;
        final int unpositionedStartIndex = calculateUnpositionedStartIndex(availableContentSpace);
        final int remainingUnpositioned = getUnpositionedButtons().size() - unpositionedStartIndex;
        final int unpositionedToAdd = Math.min(spaceRemaining, remainingUnpositioned);

        for (int i = 0; i < unpositionedToAdd; i++) {
            int buttonIndex = unpositionedStartIndex + i;
            if (buttonIndex < getUnpositionedButtons().size()) {
                inventory.addItem(getUnpositionedButtons().get(buttonIndex).refresh().get());
            }
        }
    }

    /**
     * Counts the number of filled slots in the inventory.
     *
     * @return Number of filled slots
     */
    private int countFilledSlots() {
        return (int) Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull) // null = empty slot
                .count();
    }

    /**
     * Calculates the starting index for unpositioned {@link MenuButton} based on pagination.
     * This is necessary due to the positioned {@link MenuButton}s.
     *
     * @param availableContentSpace Number of available slots per page
     * @return Starting index for unpositioned buttons
     */
    private int calculateUnpositionedStartIndex(int availableContentSpace) {
        int unpositionedStartIndex = 0;
        // For each previous page
        for (int p = 1; p < page; p++) {
            // Calculate positioned buttons on this page
            int pageStartIndex = (p - 1) * availableContentSpace;
            int pageEndIndex = pageStartIndex + availableContentSpace;
            int pagePositionedButtonAmount = (int) getPositionedButtons().entrySet().stream()
                    .filter(entry -> entry.getKey() >= pageStartIndex && entry.getKey() < pageEndIndex)
                    .count();
            // Calculate space available for unpositioned buttons on this page
            int unpositionedSlotsOnPage = availableContentSpace - pagePositionedButtonAmount;
            // Add to running total of unpositioned buttons to skip
            unpositionedStartIndex += unpositionedSlotsOnPage;
        }
        return unpositionedStartIndex;
    }
}