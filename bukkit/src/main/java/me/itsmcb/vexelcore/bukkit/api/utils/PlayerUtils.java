package me.itsmcb.vexelcore.bukkit.api.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkinInformation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class PlayerUtils {

    public static PlayerSkinInformation setAnotherSkin(Player player, String existingPlayer, JavaPlugin pluginInstance) {
        PlayerProfile playerProfile = player.getPlayerProfile();
        PlayerSkinInformation skinInformation = new PlayerSkinInformation(existingPlayer);
        // Run initial API calls asynchronously to not block main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skinInformation.isInformationComplete()) {
                    playerProfile.setProperty(new ProfileProperty("textures", skinInformation.getSkinValue(), skinInformation.getSkinSignature()));
                }
                // Then run the following code synchronously (have to due to Bukkit method calls)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (skinInformation.isInformationComplete()) {
                            player.setPlayerProfile(playerProfile);
                        }
                    }
                }.runTask(pluginInstance);
            }
        }.runTaskAsynchronously(pluginInstance);
        return skinInformation;
    }

    public static void setAnotherSkin(Player player, String value, String signature) {
        PlayerProfile playerProfile = player.getPlayerProfile();
        playerProfile.setProperty(new ProfileProperty("textures", value, signature));
        player.setPlayerProfile(playerProfile);
    }

}
