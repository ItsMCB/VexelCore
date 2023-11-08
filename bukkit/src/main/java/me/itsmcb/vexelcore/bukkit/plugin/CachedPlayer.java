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

    public static PlayerSkin bedrockDefault = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTYxNjYwMDc4Mzc1NywKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc2MTVjYTJlMWU4ZmVlZDcxYTQ3YzQ1NWM2MGM0NjEzMjY1NTdlZWI3YzRlNTYwYjZiOGYwMDY1YTMxNzgzNGYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            "R+6skz5tHQtnqqsSZyEAWIFSejKFcGoBynqR5SymlzLefLPwFL1JsbXJkpsAg2HR4jSvXoUl45AeyQ8rIET+D0d0S1W6zhlPqLYikl8GVuKgsUV+DuTLTWSXLq8sub/n3+HjivHjLZSN5udJdI9J4iA0QNwe/ftdutut1p5cRW65nbb0kPAedFM+VoWzICXHhPa6aFOC36pqI1ZJVThm+xhDHo0U0MUID/gA98va4xkGB2AWyUn3fxDTjA1IQ1ItDnDNJoXQv3+Duce+ZakaSjkZGReApE4Q/ygsGWRiOHquJpGS6fXAaPga2LbNX8lVXxgfkKfnu4TmnqxPwie0TZMxIPHoGPt9vnepRS/JFH3A12OqUHlLBEigtNRWQqeTlVJsX0+Gy16DVmguSPh7St3Y3zjuwUe0C3zyuBiMuqHBjYRwagQ0UhwmIZlCsYQYahUv2XroxguBaLwhnvb/WEcDaYqj23IViMUhsHbu0h02l+qIvG98OVXW8ZbY6gcFTF3a7+uFsmqKmiSkeCT9vUU9HWkhVqeRmdq1vI9Uq/FQRcW4KSSWMuSVk+8u0nM15lJWddqgtkJVVJxEoYWja1zIOmgXBxpXZHpkNCM8NHixWQt9bnsEABLyP9lwgL9zpkFDAo8WDPS1wWDAUhmpx0yh/9JrXWz5exoizATebLQ="
    );

    public void setBedrockSkin() {
        if (playerSkin.isComplete()) {
            return;
        }
        setPlayerSkin(bedrockDefault);
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
            if (p.getName() != null) {
                lhm.put("name", p.getName());
            }
            if (p.getUUID() != null) {
                lhm.put("uuid", p.getUUID().toString());
            }
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
            String uuidStr = (String) map.get("uuid");
            if (uuidStr != null) {
                cachedPlayer.setUUID(UUID.fromString(uuidStr));
            }
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
