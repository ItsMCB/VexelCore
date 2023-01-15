package me.itsmcb.vexelcore.bukkit.api.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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

}
