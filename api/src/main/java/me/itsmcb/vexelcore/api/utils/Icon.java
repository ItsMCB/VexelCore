package me.itsmcb.vexelcore.api.utils;

public enum Icon {

    DOUBLE_ARROW_RIGHT('»'),
    DOUBLE_ARROW_LEFT('«'),
    STAR('★'),
    DIAMOND('✦'),
    TICK('✔'),
    CROSS('✘'),
    HEART('❤'),
    SMILEY_FACE('☻'),
    DOT('●');

    private char icon;

    Icon(char icon) {
        this.icon = icon;
    }

    public char getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return String.valueOf(getIcon());
    }

}
