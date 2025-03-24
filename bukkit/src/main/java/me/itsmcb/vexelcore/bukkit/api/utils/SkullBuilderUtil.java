package me.itsmcb.vexelcore.bukkit.api.utils;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.itsmcb.vexelcore.bukkit.api.cache.PlayerSkinData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkullBuilderUtil {
    private String texture;
    private String signature;

    public SkullBuilderUtil(@NotNull String texture) {
        this.texture = texture;
    }

    public SkullBuilderUtil(@NotNull String texture, @NotNull String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public SkullBuilderUtil(@NotNull PlayerSkinData playerSkinData) {
        this.texture = playerSkinData.getTexture();
        this.signature = playerSkinData.getSignature();
    }

    public ItemStack get() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        try {
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID(), null);
            playerProfile.setProperty(new ProfileProperty("textures",texture,signature));
            skullMeta.setPlayerProfile(playerProfile);
            item.setItemMeta(skullMeta);
            return item;
        } catch (Exception ex) {
            throw new NullPointerException("[SkullBuilder] Failed to build skull with texture: "+texture);
        }
    }
}
