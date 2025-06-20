package me.itsmcb.vexelcore.bukkit.api.menu;

public enum MenuRowSize {
    ONE(9),
    TWO(18),
    THREE(27),
    FOUR(36),
    FIVE(45),
    SIX(54);

    private final int size;

    MenuRowSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public static boolean isValidSize(int size) {
        return size > 0 && size % 9 == 0;
    }

    /**
     * Determines the appropriate menu size based on content count.
     * Reserves the bottom row (9 slots) for navigation buttons.
     *
     * @param contentCount The number of content items to display
     * @return The MenuRowSize that can accommodate the content plus navigation
     */
    public static MenuRowSize getMenuSizeForContent(int contentCount) {
        // No content
        if (contentCount <= 0) {
            return ONE;
        }

        // Calculate required slots (content + bottom navigation)
        int requiredSlots = contentCount + 9;

        // Determine the smallest reasonable size
        for (MenuRowSize size : values()) {
            if (size.getSize() >= requiredSlots) {
                return size;
            }
        }

        // If content exceeds even the largest menu size, return the largest
        return SIX;
    }
}