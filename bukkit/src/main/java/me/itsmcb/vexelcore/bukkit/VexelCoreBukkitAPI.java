package me.itsmcb.vexelcore.bukkit;

import java.util.List;
import java.util.UUID;

public class VexelCoreBukkitAPI {

    public static String getPlayerBrandCache(UUID playerUUID) {
        return VexelCoreBukkit.getInstance().getProxyManager().getPlayerBrand(playerUUID);
    }

    public static boolean serverIsConnectedToProxy() {
        return VexelCoreBukkit.getInstance().getProxyManager().getServerNames().size() > 0;
    }

    public static List<String> getProxyServerNamesCache() {
        return VexelCoreBukkit.getInstance().getProxyManager().getServerNames();
    }

    public static void refreshProxyServerNameCache() {
        VexelCoreBukkit.getInstance().getProxyManager().refreshServerNames();
    }

}
