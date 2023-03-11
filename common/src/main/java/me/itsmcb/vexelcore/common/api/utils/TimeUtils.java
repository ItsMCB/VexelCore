package me.itsmcb.vexelcore.common.api.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {

    public static String convert(final long time){
        return convert(time,"yyyy MM dd HH:mm:ss");
    }

    public static String convert(final long time, String pattern){
        return new SimpleDateFormat(pattern).format(new Date(time));
    }

    public static String conciseEpochDateFromSeconds(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM d, uuuu 'at' h:mm a");
        return dateTime.format(formatter);
    }

    public static String conciseEpochDateFromMilliseconds(long epochMilliseconds) {
        Instant instant = Instant.ofEpochMilli(epochMilliseconds);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM d, uuuu 'at' h:mm a");
        return dateTime.format(formatter);
    }

}
