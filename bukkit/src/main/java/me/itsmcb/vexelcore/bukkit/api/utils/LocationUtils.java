package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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

    // Based on https://www.spigotmc.org/threads/spawn-armor-stand-relative-to-player-location.473159/#post-4001540
    public static void entityLookAtPlayer(Player p, Entity entity) {
        Vector direction = entity.getLocation().toVector().subtract(p.getEyeLocation().toVector()).normalize();
        double x = direction.getX();
        double y = direction.getY();
        double z = direction.getZ();

        Location changed = entity.getLocation().clone();
        changed.setYaw(180 - toDegree(Math.atan2(x, z)));
        changed.setPitch(90 - toDegree(Math.acos(y)));
        entity.teleport(changed);
    }

    public static float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }

}
