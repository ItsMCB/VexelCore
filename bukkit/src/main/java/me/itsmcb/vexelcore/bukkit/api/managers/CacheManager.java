package me.itsmcb.vexelcore.bukkit.api.managers;

import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import me.itsmcb.vexelcore.bukkit.plugin.CachedPlayer;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerInformation;
import me.itsmcb.vexelcore.common.api.web.mojang.PlayerSkin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
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
        // Check if in cache
        Optional<CachedPlayer> optional = get(name);
        if (optional.isPresent()) {
            return optional.get();
        }
        // Create new cached player object
        CachedPlayer cachedPlayer = createCacheFromBukkitCache(Bukkit.getOfflinePlayer(name));
        if (cachedPlayer.isComplete()) {
            return cachedPlayer;
        }
        // Information is missing. Call Mojang or Floodgate API
        // Determine if Bedrock or Java
        FloodgateApi api = FloodgateApi.getInstance();
        cachedPlayer.setName(name);
        if (name.contains(api.getPlayerPrefix())) {
            // Is Bedrock
            try {
                // Set UUID
                cachedPlayer.setUUID(api.getUuidFor(name.substring(api.getPlayerPrefix().length())).get());
                // Set skin
                cachedPlayer.setBedrockSkin();
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

    public CachedPlayer request(Player player) {
        return request(player.getUniqueId());
    }

    private long oneDayTTL = 86400000;

    private Optional<CachedPlayer> get(UUID uuid) {
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        Optional<CachedPlayer> optional = cache.stream().filter(p -> p != null && p.getUUID() != null && p.getUUID().equals(uuid)).findFirst();
        optional.ifPresent(this::clearIfOld);
        return optional;
    }

    private Optional<CachedPlayer> get(String name) {
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        Optional<CachedPlayer> optional = cache.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
        optional.ifPresent(this::clearIfOld);
        return optional;
    }

    private CachedPlayer createCacheFromBukkitCache(OfflinePlayer offlinePlayer) {
        CachedPlayer cachedPlayer = new CachedPlayer();
        // Set TTL to be between 1 and 7 days. This ensures that the cache won't get invalidated at the same time thus avoiding rate limiting problems
        cachedPlayer.setTTL(oneDayTTL+ oneDayTTL *new Random().nextInt(7));
        // Check if cached on server
        if (offlinePlayer.hasPlayedBefore()) {
            cachedPlayer = new CachedPlayer(offlinePlayer.getPlayerProfile());
            if (offlinePlayer.getName() != null) {
                cachedPlayer.setName(offlinePlayer.getName());
            }
            if (offlinePlayer.getUniqueId().toString() != null) {
                cachedPlayer.setUUID(offlinePlayer.getUniqueId());
            }
        }
        return cachedPlayer;
    }

    private void setFloodgateTTLIfComplete(CachedPlayer cachedPlayer) {
        // Extend bedrock TTL if all data is present because data can't be fetched after they log off like Java
        if (cachedPlayer.isComplete()) {
            cachedPlayer.setTTL(oneDayTTL*365);
        } else {
            // 1 min TTL
            cachedPlayer.setTTL(oneDayTTL/24/60);
        }
    }

    public CachedPlayer request(UUID uuid) {
        // Check if in cache
        Optional<CachedPlayer> optional = get(uuid);
        if (optional.isPresent()) {
            return optional.get();
        }
        // Create new cached player object
        CachedPlayer cachedPlayer = createCacheFromBukkitCache(Bukkit.getOfflinePlayer(uuid));
        if (cachedPlayer.isComplete()) {
            return cachedPlayer;
        }
        // Information is missing. Call Mojang or Floodgate API
        // Determine if Bedrock or Java
        FloodgateApi api = FloodgateApi.getInstance();
        //cachedPlayer.setUUID(uuid);
        if (api.isFloodgateId(uuid)) { // Is Bedrock
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
                }
            }
            // Set skin
            cachedPlayer.setBedrockSkin();
            setFloodgateTTLIfComplete(cachedPlayer);
        } else {
            // Is Java
            PlayerInformation playerInformation = new PlayerInformation(uuid);
            cachedPlayer.setName(playerInformation.getName());
            cachedPlayer.setUUID(uuid);
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


    private void addToCache(CachedPlayer cachedPlayer) {
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        cache.add(cachedPlayer);
        playerCacheConfig.get().set("cache",cache);
        playerCacheConfig.save();
    }

    private void clearIfOld(CachedPlayer cachedPlayer) {
        if (System.currentTimeMillis()-cachedPlayer.getLastRefresh() > cachedPlayer.getTTL()) {
            // Remove if old
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
