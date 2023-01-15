package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import java.util.List;

public class BukkitUtils {

    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
    }

    public static boolean isOnlinePlayer(String name) {
        return (Bukkit.getPlayer(name) != null);
    }
}
