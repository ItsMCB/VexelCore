package me.itsmcb.vexelcore.common.api.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {

    public static String formatSecondsToTime(int seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = ((seconds % 86400) % 3600) / 60;

        StringBuilder formattedTime = new StringBuilder();
        if (days > 0) {
            formattedTime.append(days).append("d ");
        }
        if (hours > 0) {
            formattedTime.append(hours).append("h ");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append("m");
        }

        return formattedTime.toString().trim();
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
