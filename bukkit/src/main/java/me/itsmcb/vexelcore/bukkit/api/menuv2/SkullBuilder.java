package me.itsmcb.vexelcore.bukkit.api.menuv2;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkinInformation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SkullBuilder extends ItemBuilder{

    private String texture;

    public SkullBuilder(String texture) {
        super(Material.PLAYER_HEAD);
        this.texture = texture;
    }

    public SkullBuilder(Player player) {
        super(Material.PLAYER_HEAD);
        player.getPlayerProfile().getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                texture = profileProperty.getValue();
            }
        });
    }

    public SkullBuilder(OfflinePlayer offlinePlayer) {
        super(Material.PLAYER_HEAD);
        texture = new PlayerSkinInformation(offlinePlayer.getName()).getSkinValue();
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack skull = super.getItemStack();
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        try {
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID(), null);
            playerProfile.setProperty(new ProfileProperty("textures",texture));
            playerProfile.update();
            skullMeta.setPlayerProfile(playerProfile);
            skull.setItemMeta(skullMeta);
        } catch (Exception ex) {
            throw new NullPointerException("Player skin texture couldn't be found. Are they a bedrock player with an unloaded skin? It's safe to ignore this error.");
        }
        return skull;
    }
}
