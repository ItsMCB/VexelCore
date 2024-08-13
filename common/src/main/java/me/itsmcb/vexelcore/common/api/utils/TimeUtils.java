package me.itsmcb.vexelcore.common.api.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {

    public static String formatSecondsToTime(long seconds) {
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            result.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            result.append(minutes).append("m ");
        }
        result.append(seconds).append("s");

        return result.toString().trim();
    }

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

    public static String concise2EpochDateFromMilliseconds(long epochMilliseconds) {
        Instant instant = Instant.ofEpochMilli(epochMilliseconds);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy 'at' h:mm a");
        return dateTime.format(formatter);
    }

}
