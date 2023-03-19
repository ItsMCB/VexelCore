package me.itsmcb.vexelcore.bukkit.api.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkinInformation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class PlayerUtils {

    public static PlayerSkinInformation setRealSkin(Player from, String to, JavaPlugin pluginInstance) {
        PlayerProfile playerProfile = from.getPlayerProfile();
        PlayerSkinInformation skinInformation = new PlayerSkinInformation(to);
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

    public static void setRealSkin(Player player, String value, String signature) {
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

    public static void copy(Player from, Player to) {
        from.getPlayerProfile().getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                PlayerUtils.setRealSkin(to, profileProperty.getValue(), profileProperty.getSignature());
                Msg.send(to, "&bUpdated skin to display as &b"+from.getName());
            }
        });
    }

}
