package me.itsmcb.vexelcore.common.api.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    public static String convert(final long time){
        return convert(time,"yyyy MM dd HH:mm:ss");
    }

    public static String convert(final long time, String pattern){
        return new SimpleDateFormat(pattern).format(new Date(time));
    }

}
