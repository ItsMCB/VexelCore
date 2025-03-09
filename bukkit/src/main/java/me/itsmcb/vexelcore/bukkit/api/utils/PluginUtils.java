package me.itsmcb.vexelcore.bukkit.api.utils;

import me.itsmcb.vexelcore.bukkit.api.cache.GeyserUtils;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static org.bukkit.Bukkit.getServer;

/**
 * Easily get valid instances of plugin APIs.
 * <p>
 * Looking for a Geyser Floodgate check? Use {@link GeyserUtils#floodgateIsInstalled()}
 *
 */
public class PluginUtils {

    /**
     * Gets the CoreProtect API
     *
     * @return A valid CoreProtectAPI instance or null
     */
    public static CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        return CoreProtect;
    }

    /**
     * Determines if the Throwable or its cause is an instance of any classes provided.
     * <p>
     * This was made to easily check for the type of exception provided in {@link java.util.concurrent.CompletableFuture#exceptionally(Function)}.
     *
     * @param t The throwable to check
     * @param ee The classes to check against
     * @return {@code true} if the throwable or its cause is an instance of any provided classes; {@code false} otherwise.
     */
    public static boolean throwableIs(@NotNull Throwable t, @NotNull Class<?>... ee) {
        Throwable current = t;
        while (current != null) {
            for (Class<?> cls : ee) {
                if (cls.isInstance(current)) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Determines if a plugin is currently loaded by asking the Bukkit PluginManager
     */
    public static boolean pluginIsLoaded(@NotNull String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin != null) {
            return plugin.isEnabled();
        }
        return false;
    }
}
