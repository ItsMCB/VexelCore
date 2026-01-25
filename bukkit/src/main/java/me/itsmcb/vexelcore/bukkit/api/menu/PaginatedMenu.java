package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.VexelCoreBukkitAPI;
import me.itsmcb.vexelcore.common.api.HeadTexture;
import org.bukkit.Material;
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
    private BackNavigationButton back = new BackNavigationButton(this);
    private ForwardNavigationButton forward = new ForwardNavigationButton(this);

    public PaginatedMenu(MenuRowSize size, String title) {
        super(size, title);
        addPaginatedButtons();
    }

    /**
     * Add navigation {@link MenuButton}s to template.
     */
    private void addPaginatedButtons() {
        MenuButton blankGlassPane = new MenuButton(Material.BLACK_STAINED_GLASS_PANE).name("&7").setHideToolTip(true);
        // Left
        setTemplateButton(getSize().getSize()-9,blankGlassPane);
        setTemplateButton(getSize().getSize()-8,blankGlassPane);
        // Previous Arrow
        setTemplateButton(getSize().getSize()-7, back);

        setTemplateButton(getSize().getSize()-6,blankGlassPane);
        setTemplateButton(getSize().getSize()-4,blankGlassPane);

        // Forward Arrow
        setTemplateButton(getSize().getSize()-3, forward);
        // Right
        setTemplateButton(getSize().getSize()-2,blankGlassPane);
        setTemplateButton(getSize().getSize()-1,blankGlassPane);
    }

    /**
     * Sets a button in the bottom navigation toolbar (last 9 slots).
     *
     * @param index  The relative index in the toolbar (0-8).
     * @param button The button to place.
     * @return This PaginatedMenu instance.
     * @throws IllegalArgumentException if the index is not between 0 and 8.
     */
    public PaginatedMenu setToolbarButton(int index, MenuButton button) {
        if (index < 0 || index > 8) {
            throw new IllegalArgumentException("Toolbar index must be between 0 and 8.");
        }
        int absoluteIndex = getSize().getSize() - 9 + index;
        setTemplateButton(absoluteIndex, button);
        return this;
    }

    public int getPage() {
        return page;
    }

    public boolean pageCanGoBack() {
        // Check if the current page is already at the first page
        if (page <= 1) {
            page = 1;
            return false;
        }
        return true;
    }

    public void pageGoBack() {
        if (!pageCanGoBack()) {
            return;
        }
        // Go to the previous page
        page--;
        setMenuUpdateRequested(true);
        refresh();
    }

    private int calculateMenuContentSize() {
        return getPositionedButtons().size() + getUnpositionedButtons().size();
    }

    private int calculateAvailableInventorySpace() {
        return getSize().getSize() - getTemplatePositionedButtons().size();
    }

    public int getTotalPages() {
        int menuContentSize = calculateMenuContentSize();
        int availableInventorySpace = calculateAvailableInventorySpace();
        return (int) Math.ceil((double) menuContentSize / availableInventorySpace);
    }

    public boolean isLastPage() {
        return page >= getTotalPages();
    }

    public void pageGoForward() {
        if (isLastPage()) {
            return;
        }
        page++;
        setMenuUpdateRequested(true);
        refresh();
    }

    @Override
    public void rebuildMenu() {
        inventory.clear();
        final int inventorySize = getSize().getSize();
        final int templateButtonSize = getTemplatePositionedButtons().size();
        final int availableButtonSlots = inventorySize - templateButtonSize;

        getTemplatePositionedButtons().forEach((i,b) -> b.refresh()); // Ensure ALL template and positioned buttons update if needed (some may not be refreshed via method below)
        applyTemplateAndPositionedButtons();
        int filledSlots = applyPositionedButtons(availableButtonSlots);
        getUnpositionedButtons().forEach(MenuButton::refresh); // Ensure ALL unpositioned buttons update if needed (some may not be refreshed via method below)
        applyUnpositionedButtons(inventorySize, filledSlots);
    }

    /**
     * Applies template buttons to the inventory.
     */
    private void applyTemplateAndPositionedButtons() {
        int middle = getSize().getSize()-5;
        if (VexelCoreBukkitAPI.getMenuManager().hasPreviousMenu(this)) {
            setTemplateButton(middle, new MenuButton(HeadTexture.GRAY_ARROW_DOWN.getTexture()).name("&f&lBack").click(e -> {
                VexelCoreBukkitAPI.getMenuManager().openPrevious(this, ((Player) e.getWhoClicked()).getPlayer());
            }));
        } else {
            setTemplateButton(middle, new MenuButton(HeadTexture.GRAY_X.getTexture()).name("&f&lExit").addLore("","&7➤ &f&nClick to close menu").click(e -> {
                e.getWhoClicked().closeInventory();
            }));
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