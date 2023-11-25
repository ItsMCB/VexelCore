package me.itsmcb.vexelcore.bukkit.api.menuv2;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.itsmcb.vexelcore.bukkit.plugin.CachedPlayer;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerInformation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SkullBuilder extends MenuV2Item {

    private String texture;
    private String signature;

    public SkullBuilder(String texture) {
        super(Material.PLAYER_HEAD);
        this.texture = texture;
    }

    public SkullBuilder(String texture, String signature) {
        super(Material.PLAYER_HEAD);
        this.texture = texture;
        this.signature = signature;
    }

    public SkullBuilder(CachedPlayer cachedPlayer) {
        super(Material.PLAYER_HEAD);
        this.texture = cachedPlayer.getPlayerSkin().getValue();
        this.signature = cachedPlayer.getPlayerSkin().getSignature();
    }

    public SkullBuilder(Player player) {
        super(Material.PLAYER_HEAD);
        player.getPlayerProfile().getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                texture = profileProperty.getValue();
                signature = profileProperty.getSignature();
            }
        });
    }

    public SkullBuilder(OfflinePlayer offlinePlayer) {
        super(Material.PLAYER_HEAD);
        texture = new PlayerInformation(offlinePlayer.getUniqueId()).getPlayerSkin().getValue();
        signature = new PlayerInformation(offlinePlayer.getUniqueId()).getPlayerSkin().getSignature();
    }

    @Override
    public SkullBuilder update() {
        setHeadMeta();
        return this;
    }

    private void setHeadMeta() {
        TextComponent.Builder tc = Component.text();
        tc.append(displayName());
        SkullMeta skullMeta = (SkullMeta) getItemMeta();
        try {
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID(), null);
            playerProfile.setProperty(new ProfileProperty("textures",texture,signature));
            skullMeta.setPlayerProfile(playerProfile);
            setItemMeta(skullMeta);
        } catch (Exception ex) {
            throw new NullPointerException("Player skin texture couldn't be found. Are they a bedrock player with an unloaded skin? It's safe to ignore this error.");
        }
    }
}
