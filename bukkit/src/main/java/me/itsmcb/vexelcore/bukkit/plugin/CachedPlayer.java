package me.itsmcb.vexelcore.bukkit.plugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CachedPlayer {
    private String name;
    private UUID uuid;
    private PlayerSkin playerSkin = new PlayerSkin("","");
    private long lastRefresh = System.currentTimeMillis();
    private long ttl = 86400000;

    public CachedPlayer() {}
    public CachedPlayer(UUID uuid) {
        //this.uuid = uuid;
        this(Bukkit.getOfflinePlayer(uuid).getPlayerProfile());
    }
    public CachedPlayer(String name) {
        //this.name = name;
        this(Bukkit.getOfflinePlayer(name).getPlayerProfile());
    }

    public CachedPlayer(PlayerProfile playerProfile) {
        setName(playerProfile.getName());
        setUUID(playerProfile.getId());
        playerProfile.getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                setPlayerSkin(new PlayerSkin(profileProperty.getValue(),profileProperty.getSignature()));
            }
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerSkin getPlayerSkin() {
        return playerSkin;
    }

    public void setPlayerSkin(PlayerSkin playerSkin) {
        this.playerSkin = playerSkin;
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(long lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public long getTTL() {
        return ttl;
    }

    public void setTTL(long ttl) {
        this.ttl = ttl;
    }

    public boolean isComplete() {
        return this.name != null && this.uuid != null && this.playerSkin.hasValue() && this.playerSkin.hasSignature();
    }

    public static TypeAdapter<CachedPlayer> adapter = new TypeAdapter<>() {

        @NotNull
        @Override
        public Map<Object, Object> serialize(@NotNull CachedPlayer p) {
            LinkedHashMap<Object, Object> lhm = new LinkedHashMap<>();
            lhm.put("name", p.getName());
            lhm.put("uuid", p.getUUID().toString());
            lhm.put("last", p.getLastRefresh());
            lhm.put("llt",p.getTTL());
            if (p.playerSkin != null) {
                lhm.put("value", p.getPlayerSkin().getValue());
                lhm.put("signature", p.getPlayerSkin().getSignature());
            }
            return lhm;
        }

        @NotNull
        @Override
        public CachedPlayer deserialize(@NotNull Map<Object, Object> map) {
            CachedPlayer cachedPlayer = new CachedPlayer();
            cachedPlayer.setName((String) map.get("name"));
            cachedPlayer.setUUID(UUID.fromString((String) map.get("uuid")));
            cachedPlayer.setLastRefresh((long) map.get("last"));
            if (map.containsKey("ttl")) {
                cachedPlayer.setTTL(Long.parseLong((String) map.get("llt")));
            }
            if (map.containsKey("value")) {
                cachedPlayer.setPlayerSkin(new PlayerSkin(
                        (String) map.get("value"),
                        (String) map.get("signature")
                ));
            }
            return cachedPlayer;
        }
    };
}
