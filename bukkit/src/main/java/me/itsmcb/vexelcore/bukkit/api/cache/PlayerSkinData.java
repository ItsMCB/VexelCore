package me.itsmcb.vexelcore.bukkit.api.cache;

import org.bukkit.entity.Player;

/**
 * Represents Minecraft player skin data
 */
public class PlayerSkinData {

    private String texture;
    private String signature;

    /**
     * Creates new player skin data from online player
     *
     * @param player Player to copy skin data from
     */
    public PlayerSkinData(Player player) {
        player.getPlayerProfile().getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                texture = profileProperty.getValue();
                signature = profileProperty.getSignature();
            }
        });
        setDefaultIfNull();
    }

    /**
     * Set default skin value if no valid texture plus signature combinations are set
     */
    private void setDefaultIfNull() {
        if (texture != null && signature != null) {
            return;
        }
        this.texture = CacheManagerV2.DEFAULT_STEVE_TEXTURE;
        this.signature = CacheManagerV2.DEFAULT_STEVE_SIGNATURE;
    }

    /**
     * Creates new player skin data with the provided texture and signature
     *
     * @param texture Skin texture data
     * @param signature Skin signature data
     */
    public PlayerSkinData(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
        setDefaultIfNull();
    }

    /**
     * Creates new player texture data with default Steve skin
     */
    public PlayerSkinData() {
        this(null, null);
    }

    /**
     * Gets the skin texture data
     *
     * @return the skin texture data
     */
    public String getTexture() {
        return texture;
    }

    /**
     * Sets the skin texture data
     *
     * @param texture the new skin texture data
     */
    public void setTexture(String texture) {
        this.texture = texture;
    }

    /**
     * Gets the skin signature data
     *
     * @return the skin signature data
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the skin signature data
     *
     * @param signature the new skin signature data
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Checks if the skin texture has a signature
     *
     * @return true if both texture and signature are not null
     */
    public boolean hasValidSkin() {
        return texture != null && signature != null;
    }

    @Override
    public String toString() {
        return "PlayerTextureData{" +
                "texture='" + texture + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}