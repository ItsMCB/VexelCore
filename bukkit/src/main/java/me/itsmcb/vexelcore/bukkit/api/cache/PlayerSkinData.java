package me.itsmcb.vexelcore.bukkit.api.cache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.URL;
import java.util.Base64;

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

    /**
     * Retrieves the skin URL from the texture data.
     *
     * @return The skin as a {@link URL} object or {@code null} if the texture doesn't exist or could not be parsed.
     */
    public URL getSkinUrl() {
        if (this.texture == null) {
            return null;
        }
        try {
            // Decode the Base64 texture value to plain text and parse it as JSON
            byte[] decodedTextureBytes = Base64.getDecoder().decode(this.texture);
            JsonObject jsonObject = JsonParser.parseString(new String(decodedTextureBytes)).getAsJsonObject();

            // Navigate through the JSON to the skin URL
            JsonObject texturesObject = jsonObject.getAsJsonObject("textures");
            if (texturesObject == null) {
                return null;
            }
            JsonObject skinObject = texturesObject.getAsJsonObject("SKIN");
            if (skinObject == null) {
                return null;
            }
            return URI.create(skinObject.get("url").getAsString()).toURL();

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "PlayerTextureData{" +
                "texture='" + texture + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}