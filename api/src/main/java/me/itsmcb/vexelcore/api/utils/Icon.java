package me.itsmcb.vexelcore.api.utils;

public enum Icon {

    DOUBLE_ARROW_RIGHT('»'),
    DOUBLE_ARROW_LEFT('«'),
    STAR('★'),
    CIRCLE('●'),
    SQUARE('■'),
    TRIANGLE_POINTING_UP('▲'),
    TRIANGLE_POINTING_DOWN('▼'),
    DIAMOND('✦'),
    TICK('✔'),
    CROSS('✘'),
    HEART('❤'),
    PICKAXE('⛏'),
    CROSSED_SWORDS('⚔'),
    ENVELOPE('✉'),
    FLOWER('❀'),
    SMILEY_FACE('☻'),
    RADIOACTIVE('☢'),
    FRACTION_ONE_HALF('½'),
    FILLED_CIRCLE_1('➊'),
    FILLED_CIRCLE_2('➋'),
    FILLED_CIRCLE_3('➌'),
    FILLED_CIRCLE_4('➍'),
    FILLED_CIRCLE_5('➎'),
    FILLED_CIRCLE_6('➏'),
    FILLED_CIRCLE_7('➐'),
    FILLED_CIRCLE_8('➑'),
    FILLED_CIRCLE_9('➒'),
    FILLED_CIRCLE_10('➓'),
    FILLED_CIRCLE_11('⓫'),
    FILLED_CIRCLE_12('⓬'),
    FILLED_CIRCLE_13('⓭'),
    FILLED_CIRCLE_14('⓮'),
    FILLED_CIRCLE_15('⓯'),
    FILLED_CIRCLE_16('⓰'),
    FILLED_CIRCLE_17('⓱'),
    FILLED_CIRCLE_18('⓲'),
    FILLED_CIRCLE_19('⓳'),
    FILLED_CIRCLE_20('⓴'),
    CIRCLE_0('⓪'),
    CIRCLE_1('➀'),
    CIRCLE_2('➁'),
    CIRCLE_3('➂'),
    CIRCLE_4('➃'),
    CIRCLE_5('➄'),
    CIRCLE_6('➅'),
    CIRCLE_7('➆'),
    CIRCLE_8('➇'),
    CIRCLE_9('➈'),
    CIRCLE_10('➉'),
    CIRCLE_11('⑪'),
    CIRCLE_12('⑫'),
    CIRCLE_13('⑬'),
    CIRCLE_14('⑭'),
    CIRCLE_15('⑮'),
    CIRCLE_16('⑯'),
    CIRCLE_17('⑰'),
    CIRCLE_18('⑱'),
    CIRCLE_19('⑲'),
    CIRCLE_20('⑳');

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
