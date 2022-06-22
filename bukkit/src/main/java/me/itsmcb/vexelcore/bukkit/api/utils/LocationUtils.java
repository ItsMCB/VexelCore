package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.Location;

public class LocationUtils {

    public enum LocationStringStyle {
        SEMICOLON,
        SPACE
    }

    public static String getAsString(Location location, LocationStringStyle style) {
        switch (style) {
            case SEMICOLON -> {
                return location.getBlockX()+";"+ location.getBlockY()+";"+ location.getBlockZ();
            }
            case SPACE -> {
                return location.getBlockX()+" "+ location.getBlockY()+" "+ location.getBlockZ();
            }
        }
        return location.toString();
    }

}
