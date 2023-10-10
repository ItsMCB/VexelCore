package me.itsmcb.vexelcore.bukkit.api.managers;

import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import me.itsmcb.vexelcore.bukkit.plugin.CachedPlayer;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerInformation;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class CacheManager {
    private BoostedConfig playerCacheConfig;
    // TODO On player login, refresh cache data. This is especially important for Bedrock players. This would make a TTL time of -1 work because it would never updated the Bedrock player info until they login again.

    public CacheManager(JavaPlugin plugin) {
        // Save player cache
        StandardSerializer standardSerializer = StandardSerializer.getDefault();
        standardSerializer.register(CachedPlayer.class,CachedPlayer.adapter);
        this.playerCacheConfig = new BoostedConfig(new File(plugin.getDataFolder().getParentFile()+File.separator+"VexelCore"),"player_cache",null,standardSerializer);
    }

    public CachedPlayer request(String name) {
        CachedPlayer cachedPlayer = getPlayer(name);
        if (cachedPlayer.getName() != null && cachedPlayer.getUUID() != null) {
            addToCache(cachedPlayer);
        }
        return cachedPlayer;
    }

    private CachedPlayer getPlayer(String name) {
        // Check cache
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        Optional<CachedPlayer> optional = cache.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
        if (optional.isPresent()) {
            clearIfOld(optional.get());
            return optional.get();
        }
        // Check if cached on server
        CachedPlayer cachedPlayer = new CachedPlayer();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore()) {
            cachedPlayer = new CachedPlayer(offlinePlayer.getPlayerProfile());
            if (cachedPlayer.getPlayerSkin() != null) {
                return cachedPlayer;
            }
        }
        // Check if Java or Bedrock
        FloodgateApi api = FloodgateApi.getInstance();
        cachedPlayer.setName(name);
        if (name.contains(api.getPlayerPrefix())) {
            // Is Bedrock
            try {
                // Set UUID
                cachedPlayer.setUUID(api.getUuidFor(name.substring(api.getPlayerPrefix().length())).get());
                // Set skin
                setSkin(cachedPlayer,Bukkit.getOfflinePlayer(name));
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            // Is Java
            PlayerInformation playerInformation = new PlayerInformation(name);
            cachedPlayer.setUUID(playerInformation.getUuid());
            cachedPlayer.setPlayerSkin(playerInformation.getPlayerSkin());
        }
        return cachedPlayer;
    }

    public CachedPlayer request(UUID uuid) {
        // Check if in cache
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        Optional<CachedPlayer> optional = cache.stream().filter(p -> p.getUUID().equals(uuid)).findFirst();
        if (optional.isPresent()) {
            clearIfOld(optional.get());
            return optional.get();
        }
        // Check if cached on server
        CachedPlayer cachedPlayer = new CachedPlayer();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.hasPlayedBefore()) {
            cachedPlayer = new CachedPlayer(offlinePlayer.getPlayerProfile());
            cachedPlayer.setName(offlinePlayer.getName());
        }
        // Check if Bedrock or Java
        FloodgateApi api = FloodgateApi.getInstance();
        cachedPlayer.setUUID(uuid);
        if (api.isFloodgateId(uuid)) {
            // Is Bedrock
            // Set username
            FloodgatePlayer floodgatePlayer = api.getPlayer(uuid);
            String javaUsername = "Offline Bedrock Player";
            if (floodgatePlayer != null) {
                javaUsername = floodgatePlayer.getJavaUsername();
            }
            if (floodgatePlayer != null && javaUsername != null) {
                cachedPlayer.setName(floodgatePlayer.getJavaUsername());
            } else {
                if (cachedPlayer.getName() == null) {
                    cachedPlayer.setName("Offline Bedrock Player");
                } else {
                    int oneMinMilliseconds = 60000;
                    cachedPlayer.setTTL(oneMinMilliseconds);
                }

            }
            // Set skin
            setSkin(cachedPlayer,Bukkit.getOfflinePlayer(uuid));
        } else {
            // Is Java
            PlayerInformation playerInformation = new PlayerInformation(uuid);
            cachedPlayer.setName(playerInformation.getName());
            cachedPlayer.setPlayerSkin(playerInformation.getPlayerSkin());
        }
        addToCache(cachedPlayer);
        return cachedPlayer;
    }

    public boolean isValid(String name) {
        CachedPlayer cachedPlayer = getPlayer(name);
        if (cachedPlayer.getUUID() != null) {
            return true;
        }
        PlayerInformation playerInformation = new PlayerInformation(name);
        return playerInformation.isValid();
    }

    private void setSkin(CachedPlayer cachedPlayer, OfflinePlayer offlinePlayer) {
        // Set as default just in case. Should be overridden by next line of code if found
        cachedPlayer.setPlayerSkin(new PlayerSkin(
                "ewogICJ0aW1lc3RhbXAiIDogMTYxNjYwMDc4Mzc1NywKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc2MTVjYTJlMWU4ZmVlZDcxYTQ3YzQ1NWM2MGM0NjEzMjY1NTdlZWI3YzRlNTYwYjZiOGYwMDY1YTMxNzgzNGYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "R+6skz5tHQtnqqsSZyEAWIFSejKFcGoBynqR5SymlzLefLPwFL1JsbXJkpsAg2HR4jSvXoUl45AeyQ8rIET+D0d0S1W6zhlPqLYikl8GVuKgsUV+DuTLTWSXLq8sub/n3+HjivHjLZSN5udJdI9J4iA0QNwe/ftdutut1p5cRW65nbb0kPAedFM+VoWzICXHhPa6aFOC36pqI1ZJVThm+xhDHo0U0MUID/gA98va4xkGB2AWyUn3fxDTjA1IQ1ItDnDNJoXQv3+Duce+ZakaSjkZGReApE4Q/ygsGWRiOHquJpGS6fXAaPga2LbNX8lVXxgfkKfnu4TmnqxPwie0TZMxIPHoGPt9vnepRS/JFH3A12OqUHlLBEigtNRWQqeTlVJsX0+Gy16DVmguSPh7St3Y3zjuwUe0C3zyuBiMuqHBjYRwagQ0UhwmIZlCsYQYahUv2XroxguBaLwhnvb/WEcDaYqj23IViMUhsHbu0h02l+qIvG98OVXW8ZbY6gcFTF3a7+uFsmqKmiSkeCT9vUU9HWkhVqeRmdq1vI9Uq/FQRcW4KSSWMuSVk+8u0nM15lJWddqgtkJVVJxEoYWja1zIOmgXBxpXZHpkNCM8NHixWQt9bnsEABLyP9lwgL9zpkFDAo8WDPS1wWDAUhmpx0yh/9JrXWz5exoizATebLQ="
        ));
        offlinePlayer.getPlayerProfile().getProperties().forEach(profileProperty -> {
            cachedPlayer.setPlayerSkin(new PlayerSkin(profileProperty.getValue(), profileProperty.getSignature()));
        });
    }

    private void addToCache(CachedPlayer cachedPlayer) {
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        cache.add(cachedPlayer);
        playerCacheConfig.get().set("cache",cache);
        playerCacheConfig.save();
    }

    private void clearIfOld(CachedPlayer cachedPlayer) {
        if (System.currentTimeMillis()-cachedPlayer.getLastRefresh() > cachedPlayer.getTTL()) {
            // if 3+ days old, remove
            ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
            Optional<CachedPlayer> optional = cache.stream().filter(cap -> cap.getUUID().equals(cachedPlayer.getUUID())).findFirst();
            if (optional.isPresent()) {
                cache.remove(optional.get());
            }
            playerCacheConfig.get().set("cache",cache);
            playerCacheConfig.save();
        }
    }
}
