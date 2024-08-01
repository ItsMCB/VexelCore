package me.itsmcb.vexelcore.bukkit.plugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerInformation;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        this.uuid = uuid;
    }
    public CachedPlayer(String name) {
        this(Bukkit.getOfflinePlayer(name).getPlayerProfile());
    }

    // Steve
    public static PlayerSkin incompletePlayerSkin = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTYyMTcxNTMxMjI5MCwKICAicHJvZmlsZUlkIiA6ICJiNTM5NTkyMjMwY2I0MmE0OWY5YTRlYmYxNmRlOTYwYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJpYW5hZmFnIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFhNGFmNzE4NDU1ZDRhYWI1MjhlN2E2MWY4NmZhMjVlNmEzNjlkMTc2OGRjYjEzZjdkZjMxOWE3MTNlYjgxMGIiCiAgICB9CiAgfQp9",
            "otpbxDm9B+opW7jEzZF8BVDeZSqaqdF0dyLlnlyMh7Q5ysJFDL48/9J/IOHp8JqNm1oarmVdvxrroy9dlNI2Mz4BVuJM2pcCOJwk2h+aZ4dzNZGxst+MYNPSw+i4sMoYu7OV07UVHrQffolFF7MiaBUst1hFwM07IpTE6UtIQz4rqWisXe9Iz5+ooqX4wj0IB3dPntsh6u5nVlL8acWCBDAW4YqcPt2Y4CKK+KtskjzusjqGAdEO+4lRcW1S0ldo2RNtUHEzZADWQcADjg9KKiKq9QIpIpYURIoIAA+pDGb5Q8L5O6CGI+i1+FxqXbgdBvcm1EG0OPdw9WpSqAxGGeXSwlzjILvlvBzYbd6gnHFBhFO+X7iwRJYNd+qQakjUa6ZwR8NbkpbN3ABb9+6YqVkabaEmgfky3HdORE+bTp/AT6LHqEMQo0xdNkvF9gtFci7RWhFwuTLDvQ1esby1IhlgT+X32CPuVHuxEvPCjN7+lmRz2OyOZ4REo2tAIFUKakqu3nZ0NcF98b87wAdA9B9Qyd2H/rEtUToQhpBjP732Sov6TlJkb8echGYiLL5bu/Q7hum72y4+j2GNnuRiOJtJidPgDqrYMg81GfenfPyS6Ynw6KhdEhnwmJ1FJlJhYvXZyqZwLAV1c26DNYkrTMcFcv3VXmcd5/2Zn9FnZtw="
    );

    public CachedPlayer(PlayerProfile playerProfile) {
        setFromPlayerProfile(playerProfile);
    }

    private void setFromPlayerProfile(PlayerProfile playerProfile) {
        if (name == null) {
            String playerProfileName = playerProfile.getName();
            if (playerProfileName != null && !playerProfileName.isEmpty()) {
                setName(playerProfile.getName());
            }
        }
        if (uuid == null) {
            UUID playerUUID = playerProfile.getId();
            if (playerUUID != null) {
                setUUID(playerProfile.getId());
            }
        }
        if (!playerSkin.isComplete()) {
            playerProfile.getProperties().forEach(profileProperty -> {
                if (profileProperty.getSignature() != null) {
                    setPlayerSkin(new PlayerSkin(profileProperty.getValue(),profileProperty.getSignature()));
                }
            });
        }
    }

    public boolean isBedrock() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Floodgate")) {
            if (Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot")) {
                System.err.println("Hey! VexelCore-based plugins can't save Bedrock player data properly because Floodgate isn't installed.");
            }
            if (uuid.toString().contains("00000000-0000-0000")) {
                System.err.println("Bedrock player data is being requested but it cannot be fulfilled because Floodgate is missing.");
            }
            return false;
        }
        FloodgateApi floodgateApi = FloodgateApi.getInstance();
        if (uuid != null) {
            return floodgateApi.isFloodgateId(uuid);
        }
        if (name != null) {
            return name.contains(floodgateApi.getPlayerPrefix());
        }
        return false;
    }

    public CachedPlayer tryToFindMissingValues() {
        // Shortcut grab if online
        if (uuid != null) {
            Player possiblyOnlinePlayer = Bukkit.getPlayer(uuid);
            if (possiblyOnlinePlayer != null) {
                setFromPlayerProfile(possiblyOnlinePlayer.getPlayerProfile());
            }
        }
        // Bedrock
        if (isBedrock()) {
            FloodgateApi floodgateApi = FloodgateApi.getInstance();
            if (name != null) {
                try {
                    setUUID(floodgateApi.getUuidFor(name.substring(floodgateApi.getPlayerPrefix().length())).get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (uuid != null) {
                FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(uuid);
                if (floodgatePlayer != null) {
                    setName(floodgatePlayer.getJavaUsername());
                }
            }
        }
        // Java
        if (!isBedrock()) {
            if (name == null || uuid == null || playerSkin == null || !playerSkin.isComplete()) {
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
        }

        // Set Steve skin if no other value is known
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
        return this.name != null &&
                this.uuid != null &&
                this.playerSkin.isComplete();
    }

    public String debug() {
        return "--==== Profile of "+getName()+" ====--\nUUID: "+getUUID()+" | Skin Complete: "+getPlayerSkin().isComplete();
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
