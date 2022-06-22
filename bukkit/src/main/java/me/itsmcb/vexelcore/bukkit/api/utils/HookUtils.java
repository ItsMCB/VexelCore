package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class HookUtils {

    public static boolean pluginIsLoaded(@NotNull String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin != null) {
            return plugin.isEnabled();
        }
        return false;
    }

}
