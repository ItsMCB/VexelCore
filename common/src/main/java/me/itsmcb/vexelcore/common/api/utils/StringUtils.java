package me.itsmcb.vexelcore.common.api.utils;

import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {

    // TODO remove
    public static String getCommaString(List<String> strings) {
        return String.join(", ", strings);
    }

    public static boolean isUUID(String string) {
        String uuidPattern = "[0-9a-f]{8}(-[0-9a-f]{4}){4}[0-9a-f]{8}";
        return Pattern.compile(uuidPattern).matcher(string.toLowerCase()).matches();
    }

}
