package me.itsmcb.vexelcore.common.api.utils;

import java.util.List;

public class StringUtils {

    public static String getCommaString(List<String> strings) {
        return String.join(", ", strings);
    }

}
