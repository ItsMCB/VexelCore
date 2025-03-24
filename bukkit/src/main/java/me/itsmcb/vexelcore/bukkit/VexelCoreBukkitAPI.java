package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.vexelcore.bukkit.api.cache.CacheManagerV2;
import me.itsmcb.vexelcore.bukkit.api.menu.MenuManager;

import java.util.List;
import java.util.UUID;

public class VexelCoreBukkitAPI {

    public static String getPlayerBrandCache(UUID playerUUID) {
        return VexelCoreBukkit.getInstance().getProxyManager().getPlayerBrand(playerUUID);
    }

    public static boolean serverIsConnectedToProxy() {
        refreshProxyServerNameCache();
        return VexelCoreBukkit.getInstance().getProxyManager().getServerNames().size() > 0;
    }

    public static List<String> getProxyServerNamesCache() {
        return VexelCoreBukkit.getInstance().getProxyManager().getServerNames();
    }

    public static void refreshProxyServerNameCache() {
        VexelCoreBukkit.getInstance().getProxyManager().refreshServerNames();
    }

    /**
     * Gets the VexelCore Cache Manager
     *
     * @return A valid VexelCore CacheManager instance or null
     */
    public static CacheManagerV2 getCacheManager() {
        return VexelCoreBukkit.getInstance().getCacheManagerV2();
    }

    public static MenuManager getMenuManager() { return VexelCoreBukkit.getInstance().getMenuManager(); }

}
