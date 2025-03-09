package me.itsmcb.vexelcore.bukkit.api.cache;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a cached player entity with Minecraft data
 */
public class CachedPlayerV2 {

    /**
     * Represents the edition the player is playing on
     */
    public enum Edition {
        JAVA,
        BEDROCK
    }

    private final UUID uuid;
    private String username;
    private Edition edition;
    private PlayerSkinData playerSkinData;

    /**
     * Creates a new CachedPlayer
     *
     * @param uuid     Player UUID
     * @param username Player username
     * @param edition Player edition (JAVA or BEDROCK)
     * @param texture  Skin texture data
     * @param signature Skin signature data
     */
    public CachedPlayerV2(UUID uuid, String username, Edition edition, String texture, String signature) {
        this.uuid = uuid;
        this.username = username;
        this.edition = edition;
        this.playerSkinData = new PlayerSkinData(texture,signature);
    }

    /**
     * Creates a new CachedPlayer without skin data
     *
     * @param uuid     Player UUID
     * @param username Player username
     * @param edition Player edition (JAVA or BEDROCK)
     */
    public CachedPlayerV2(UUID uuid, String username, Edition edition) {
        this(uuid, username, edition, null, null);
    }

    /**
     * Gets the player's UUID
     *
     * @return the player's UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the player's username
     *
     * @return the player's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the player's username
     *
     * @param username the new username
     * @return Self for chaining
     */
    public CachedPlayerV2 setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Gets the player's edition
     *
     * @return the player's edition (JAVA or BEDROCK)
     */
    public Edition getEdition() {
        return edition;
    }

    /**
     * Sets the player's edition
     *
     * @param edition the new edition
     * @return Self for chaining
     */
    public CachedPlayerV2 setEdition(Edition edition) {
        this.edition = edition;
        return this;
    }

    /**
     * Gets the player's skin data
     *
     * @return the player's skin data
     */
    public PlayerSkinData getPlayerSkinData() {
        return playerSkinData;
    }

    /**
     * Sets the player's skin data
     *
     * @return Self for chaining
     */
    public CachedPlayerV2 setTextureData(PlayerSkinData textureData) {
        this.playerSkinData = textureData;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedPlayerV2 that = (CachedPlayerV2) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "CachedPlayer{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", edition=" + edition +
                ", hasValidSkin=" + playerSkinData.hasValidSkin() +
                ", texture=" + playerSkinData.getTexture() +
                ", signature=" + playerSkinData.getSignature() +
                '}';
    }

    public String toStringMinimized() {
        return "CachedPlayer{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", edition=" + edition +
                ", hasValidSkin=" + playerSkinData.hasValidSkin() +
                '}';
    }
}