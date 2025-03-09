package me.itsmcb.vexelcore.bukkit.api.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.itsmcb.vexelcore.bukkit.api.managers.CacheManager;
import me.itsmcb.vexelcore.bukkit.plugin.CachedPlayer;
import me.itsmcb.vexelcore.common.api.web.mojang.OnlinePlayerSkin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class PlayerUtils {

    /**
     * @deprecated Use {@link me.itsmcb.vexelcore.bukkit.api.cache.CacheManagerV2#isValidUsername(String)} instead.
     */
    @Deprecated
    public static boolean isValid(String name, CacheManager cacheManager) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
        if (offlinePlayer != null && (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore())) {
            return true;
        }
        CachedPlayer cachedPlayer = cacheManager.get(name);
        cachedPlayer.tryToFindMissingValues();
        return cachedPlayer.isComplete();
    }

    public static OnlinePlayerSkin setRealSkin(Player from, String to, JavaPlugin pluginInstance) {
        PlayerProfile playerProfile = from.getPlayerProfile();
        OnlinePlayerSkin skinInformation = new OnlinePlayerSkin(to);
        // Run initial API calls asynchronously to not block main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skinInformation.isInformationComplete()) {
                    playerProfile.setProperty(new ProfileProperty("textures", skinInformation.getPlayerSkin().getValue(), skinInformation.getPlayerSkin().getSignature()));
                }
                // Then run the following code synchronously (have to due to Bukkit method calls)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (skinInformation.isInformationComplete()) {
                            from.setPlayerProfile(playerProfile);
                        }
                    }
                }.runTask(pluginInstance);
            }
        }.runTaskAsynchronously(pluginInstance);
        return skinInformation;
    }

    public static void setRealSkin(Player from, Player to, JavaPlugin pluginInstance) {
        setRealSkin(from, to.getName(), pluginInstance);
    }

    public static void setSkin(Player player, String value, String signature) {
        player.setPlayerProfile(setTexture(player.getPlayerProfile(), value, signature));
    }

    public static PlayerProfile setTexture(PlayerProfile playerProfile, String value) {
        playerProfile.setProperty(new ProfileProperty("textures", value));
        return playerProfile;
    }

    public static PlayerProfile setTexture(PlayerProfile playerProfile, String value, String signature) {
        playerProfile.setProperty(new ProfileProperty("textures", value, signature));
        return playerProfile;
    }

    public static String[] getValueAndSignature(Player player) {
        String[] names = new String[2];
        player.getPlayerProfile().getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                names[0] = profileProperty.getValue();
                names[1] = profileProperty.getSignature();
            }
        });
        return names;
    }

    public static void copy(Player from, Player to) {
        from.getPlayerProfile().getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                setSkin(to, profileProperty.getValue(), profileProperty.getSignature());
            }
        });
    }

}
