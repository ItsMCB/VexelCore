package me.itsmcb.vexelcore.bukkit.api.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorldGuardUtilities {

    public static BlockVector3 blockVector3FromLocation(Location location) {
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

    public static List<Player> playersInRegion(ProtectedRegion region) {
        ArrayList<Player> players = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (region.contains(BukkitAdapter.adapt(player.getLocation()).toBlockPoint())) {
                players.add(player);
            }
        });
        return players;
    }

    public static boolean inARegion(Player player) {
        return getRegions(player).size() > 0;
    }

    public static boolean inRegions(Player player) {
        return getRegions(player).size() > 1;
    }

    public static List<ProtectedRegion> getRegions(Player player) {
        BlockVector3 locationVector = blockVector3FromLocation(player.getLocation());
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())) ;
        if (regionManager == null) {
            return null;
        }
        return regionManager.getApplicableRegions(locationVector).getRegions().stream().toList();
    }

    public static boolean ownsCurrentRegion(Player player) {
        BlockVector3 locationVector = blockVector3FromLocation(player.getLocation());
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())) ;
        if (regionManager == null) {
            return false;
        }
        List<ProtectedRegion> regions = regionManager.getApplicableRegions(locationVector).getRegions().stream().toList();
        if (regions.size() == 0) {
            return false;
        }
        AtomicBoolean ownsRegion = new AtomicBoolean(false);
        regions.forEach(region -> {
            if (region.getOwners().getUniqueIds().contains(player.getUniqueId())) {
                ownsRegion.set(true);
            }
        });
        return ownsRegion.get();
    }

    public static ProtectedRegion expandAllDirections(ProtectedRegion region, World world, int expansionAmount) {
        CuboidRegion newCuboidRegion = WorldEditUtils.expandCuboidRegionAllSides(region.getMaximumPoint(), region.getMinimumPoint(), world, expansionAmount);
        return setNewRegionSize(region, world, newCuboidRegion);
    }

    public static ProtectedRegion expandWalls(ProtectedRegion region, World world, int expansionAmount) {
        CuboidRegion newCuboidRegion = WorldEditUtils.expandCuboidRegionWalls(region.getMaximumPoint(), region.getMinimumPoint(), world, expansionAmount);
        return setNewRegionSize(region, world, newCuboidRegion);
    }

    public static ProtectedRegion setNewRegionSize(ProtectedRegion region, World world, CuboidRegion newCuboidRegion) {
        ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion(region.getId(), newCuboidRegion.getPos1(), newCuboidRegion.getPos2());
        newRegion.copyFrom(region);
        WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)).addRegion(newRegion);
        saveAllWorldRegions();
        return newRegion;
    }

    public static void saveAllWorldRegions() {
        WorldGuard.getInstance().getPlatform().getRegionContainer().getLoaded().forEach(regionManager -> {
            try {
                regionManager.saveChanges();
            } catch (StorageException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
