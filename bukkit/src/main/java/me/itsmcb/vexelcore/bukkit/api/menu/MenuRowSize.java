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
}