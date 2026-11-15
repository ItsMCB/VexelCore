package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class LocationUtils {

    public enum LocationStringStyle {
        SEMICOLON,
        SPACE
    }

    public static String getAsString(Location location, LocationStringStyle style) {
        if (location == null) {
            return "No Location";
        }
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

    public static Location toCenter(Location location) {
        return location.set(location.getBlockX()+0.5,location.getBlockY(),location.getBlockZ()+0.5);
    }

    /**
     * Finds the highest safe solid block location at the given X/Z coordinates,
     * centered on the block above the solid surface. Falls back to a clone of
     * the original location if no safe block is found.
     *
     * @param loc Starting location.
     * @return Centered location on top of the highest safe solid block.
     */
    public static @NotNull Location getHighestSafeLocation(@NotNull Location loc) {
        Location original = loc.clone();
        if (loc.getWorld() == null) {
            return original;
        }
        Location check = loc.toHighestLocation();
        if (isSafeSolidBlock(check)) {
            return toCenter(check.add(0, 1, 0));
        }
        for (int y = check.getBlockY(); y > check.getWorld().getMinHeight(); y--) {
            check.set(check.getX(), y, check.getZ());
            if (isSafeSolidBlock(check)) {
                return toCenter(check.add(0, 1, 0));
            }
        }
        return original;
    }

    /**
     * Determines whether the block at the given location is a safe solid surface to stand on.
     *
     * @param loc Location of the block to inspect.
     * @return {@code true} if solid and not a hazardous/non-full block type.
     */
    public static boolean isSafeSolidBlock(@NotNull Location loc) {
        String value = loc.getBlock().getType().key().value();
        if (value.contains("fence") || value.contains("trapdoor") || value.contains("button")
                || value.contains("sign") || value.contains("wall") || value.contains("bud") || value.contains("vine")
                || value.contains("ladder") || value.contains("glow") || value.contains("web") || value.contains("torch")
                || value.contains("tripwire") || value.contains("coral_fan") || value.contains("coral_wall_fan") || value.contains("fire")) {
            return false;
        }
        return loc.getBlock().getType().isSolid();
    }

}