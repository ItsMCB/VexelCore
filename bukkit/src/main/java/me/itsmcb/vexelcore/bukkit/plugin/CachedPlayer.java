package me.itsmcb.vexelcore.bukkit.plugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerInformation;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkin;
import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CachedPlayer {
    private String name;
    private UUID uuid;
    private PlayerSkin playerSkin = new PlayerSkin("","");
    private long lastRefresh = System.currentTimeMillis();
    private long ttl = defaultTTL;

    public static long defaultTTL = 86400000;

    public CachedPlayer() {}
    public CachedPlayer(UUID uuid) {
        this(Bukkit.getOfflinePlayer(uuid).getPlayerProfile());
    }
    public CachedPlayer(String name) {
        this(Bukkit.getOfflinePlayer(name).getPlayerProfile());
    }

    public static PlayerSkin bedrockDefault = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTYxNjYwMDc4Mzc1NywKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc2MTVjYTJlMWU4ZmVlZDcxYTQ3YzQ1NWM2MGM0NjEzMjY1NTdlZWI3YzRlNTYwYjZiOGYwMDY1YTMxNzgzNGYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            "R+6skz5tHQtnqqsSZyEAWIFSejKFcGoBynqR5SymlzLefLPwFL1JsbXJkpsAg2HR4jSvXoUl45AeyQ8rIET+D0d0S1W6zhlPqLYikl8GVuKgsUV+DuTLTWSXLq8sub/n3+HjivHjLZSN5udJdI9J4iA0QNwe/ftdutut1p5cRW65nbb0kPAedFM+VoWzICXHhPa6aFOC36pqI1ZJVThm+xhDHo0U0MUID/gA98va4xkGB2AWyUn3fxDTjA1IQ1ItDnDNJoXQv3+Duce+ZakaSjkZGReApE4Q/ygsGWRiOHquJpGS6fXAaPga2LbNX8lVXxgfkKfnu4TmnqxPwie0TZMxIPHoGPt9vnepRS/JFH3A12OqUHlLBEigtNRWQqeTlVJsX0+Gy16DVmguSPh7St3Y3zjuwUe0C3zyuBiMuqHBjYRwagQ0UhwmIZlCsYQYahUv2XroxguBaLwhnvb/WEcDaYqj23IViMUhsHbu0h02l+qIvG98OVXW8ZbY6gcFTF3a7+uFsmqKmiSkeCT9vUU9HWkhVqeRmdq1vI9Uq/FQRcW4KSSWMuSVk+8u0nM15lJWddqgtkJVVJxEoYWja1zIOmgXBxpXZHpkNCM8NHixWQt9bnsEABLyP9lwgL9zpkFDAo8WDPS1wWDAUhmpx0yh/9JrXWz5exoizATebLQ="
    );
    public static String incompleteOfflineBedrockPlayerName = "Unavailable Bedrock Player";
    public static String incompleteOfflineJavaPlayerName = "Name Unavailable";
    public static UUID notchUUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    public static PlayerSkin incompletePlayerSkin = new PlayerSkin("ewogICJ0aW1lc3RhbXAiIDogMTY5ODkyODg5ODkzMywKICAicHJvZmlsZUlkIiA6ICJlN2E3MzZhMjFlM2I0YzA2YmVhOGVmMjVmODg0MmJhZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWVwMDIwNiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82YWYyMTVhNDdhMjUwZDA5YTI5MDY4YTRmNGQzNzBkMWNlNzAzY2Y2ZDczYzA5YWFlNGNlOTI3ZTIwODFkZmIxIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=","jYpDbz/V9MTyJgPdyh+vDFh8qxXTmpTixknx25DtzzqK4ZU3El6xumKAQF89/rVXGi3AlAF4K50clHmqZX6/o7edaoVMh5Emqethh8M8De3ybiFv5yTTRdtganq5Iusax+wvzOZkYpzDpyNxCChKk2nM99+q/bb3AM9Cxd/cLal3n6Ct2qaM0BqsO8wdIbOSObdoF1S90W4MNjVtspD0IYKobAtzhGXHA7/NwIU7zVX6wnyggRBHORxZmpVTqz0Fr8ZuED8AaN+Jqj/dJa8IwYPQ6VJxIVY0eDmg8TtGah9/hQLVPNqK9ahWYLIhe/x34Xae+iccXJNq5e0OtJPWuK+cm6ywjwpeU4cj81EqWfDznVAoK2u92W1pKsn3v9yenZu17ySre6ehZD4GDi4hUBFIJSNMMNhukwhohdHfoNzbVUH3vUnEsl8GPJK+L+5x/cim0icXsGIsO8QSnTFW0oKU8rClZA0xtvbSAukUPt9bmCs/jGSgq+sqoKJk3pGCwpWTgjaSoKzIqWvX/VUtQOakalmIjlHCA295Srvpn3a4Gi627y+U0fG48As00snou2X7QJt1UshDbjczKAYeLjRVW/YH9DHGytQJ4+lFdXLU/EmV60EdTrzsFWOJSQUZ/39uEdRzE1QQAgZIGptPbsdlo1N5lv3mDVkDtAN5WB0=");

    public CachedPlayer(PlayerProfile playerProfile) {
        setName(playerProfile.getName());
        setUUID(playerProfile.getId());
        playerProfile.getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                setPlayerSkin(new PlayerSkin(profileProperty.getValue(),profileProperty.getSignature()));
            }
        });
    }

    public CachedPlayer finishIfNotCompleted() {
        // Check if bedrock
        FloodgateApi api = FloodgateApi.getInstance();
        boolean isBedrock = false;
        // Floodgate name check
        if (name != null && name.contains(api.getPlayerPrefix())) {
            // Is Bedrock
            isBedrock = true;
            try {
                setUUID(api.getUuidFor(name.substring(api.getPlayerPrefix().length())).get());
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            // Floodgate UUID check
            if (api.isFloodgateId(uuid)) { // Is Bedrock
                isBedrock = true;
                // Set username
                FloodgatePlayer floodgatePlayer = api.getPlayer(uuid);
                String javaUsername = "";
                if (floodgatePlayer != null) {
                    javaUsername = floodgatePlayer.getJavaUsername();
                    if (javaUsername != null) {
                        setName(floodgatePlayer.getJavaUsername());
                    } else {
                        if (getName() == null ) {
                            setName(incompleteOfflineBedrockPlayerName);
                        }
                    }
                }
            }
        }
        if (isBedrock) {
            setTTL(defaultTTL*365);
        }
        if (isComplete()) {
            return this;
        }
        // Java player
        if (!isBedrock) {
            PlayerInformation playerInformation;
            if (name != null) {
                playerInformation = new PlayerInformation(name);
            } else {
                playerInformation = new PlayerInformation(uuid);
            }
            setName(playerInformation.getName());
            setUUID(playerInformation.getUuid());
            setPlayerSkin(playerInformation.getPlayerSkin());
        }
        if (name == null) {
            setName(incompleteOfflineJavaPlayerName);
        }
        if (uuid == null) {
            setUUID(notchUUID);
        }
        if (playerSkin == null || !playerSkin.isComplete()) {
            playerSkin = incompletePlayerSkin;
        }
        return this;
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
        return this.name != null && !this.name.equals(incompleteOfflineBedrockPlayerName) && !this.name.equals(incompleteOfflineJavaPlayerName) && this.uuid != null && this.playerSkin.hasValue() && this.playerSkin.hasSignature() && this.playerSkin != bedrockDefault && this.playerSkin != incompletePlayerSkin;
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
