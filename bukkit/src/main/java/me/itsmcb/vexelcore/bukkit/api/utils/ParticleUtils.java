package me.itsmcb.vexelcore.bukkit.api.utils;

import me.itsmcb.vexelcore.common.api.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleUtils {

    public static void spawnCircle(double radius, double stepSize, Location loc, Particle type, Particle.DustOptions dustOptions) {
        double x = 0;
        double z = 0;
        for(double i=0; i<360; i+= stepSize) {
            x = (Math.sin(i) * radius) + loc.getX();
            z = (Math.cos(i) * radius) + loc.getZ();
            Location spawnLocation = new Location(loc.getWorld(), x, loc.getY(), z);
            loc.getWorld().spawnParticle(type, spawnLocation, 1, dustOptions);
        }
    }

    public static void spawnCircleAroundCuboidRegion(World world, Location loc1, Location loc2) {
        int radius = (int) loc1.distance(loc2);
        int centerX = loc1.getBlockX() + (loc2.getBlockX() - loc1.getBlockX()) / 2;
        int centerY = loc1.getBlockY() + (loc2.getBlockY() - loc1.getBlockY()) / 2;
        int centerZ = loc1.getBlockZ() + (loc2.getBlockZ() - loc1.getBlockZ()) / 2;

        for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 32) {
            for (double phi = 0; phi < Math.PI; phi += Math.PI / 32) {
                double x = centerX + radius * Math.sin(phi) * Math.cos(theta);
                double y = centerY;
                double z = centerZ + radius * Math.sin(phi) * Math.sin(theta);
                Location particleLoc = new Location(world, x, y, z);
                particleLoc.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
            }
        }
    }

    public static boolean spawnWallsAroundCuboicRegion(Location corner1, Location corner2, int amount, JavaPlugin plugin) {
        World world = corner1.getWorld();

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX()) -1;
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX()) +1;

        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ()) -1;
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ()) +1;

        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY()) -1;
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY()) +1;

        if (MathUtils.isDifferenceGreaterThan(maxX, minX, 200)) {
            System.out.println("X diff bigger than 200");
            return false;
        }
        if (MathUtils.isDifferenceGreaterThan(maxY, minY, 200)) {
            System.out.println("Y diff bigger than 200");
            return false;
        }
        if (MathUtils.isDifferenceGreaterThan(maxZ, minZ, 200)) {
            System.out.println("Z diff bigger than 200");
            return false;
        }
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int x = minX + 1; x < maxX; x++) {
                    for (int y = minY + 1; y < maxY; y++) {
                        for (int z = minZ + 1; z < maxZ; z++) {
                            if (x == minX + 1 || x == maxX - 1 || y == minY + 1 || y == maxY - 1 || z == minZ + 1 || z == maxZ - 1) {
                                Location loc = new Location(world, x + 0.5, y + 0.5, z + 0.5);
                                //System.out.println("Spawned at: " + loc.getBlockX() + " | " + loc.getBlockZ());
                                world.spawnParticle(Particle.HAPPY_VILLAGER, loc, amount);
                            }
                        }
                    }
                }
            }
        };
        runnable.runTaskAsynchronously(plugin);
        return true;
    }
}
