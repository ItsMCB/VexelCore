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

    private Optional<CachedPlayer> getFromFile(String name) {
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        Optional<CachedPlayer> optional = cache.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
        optional.ifPresent(this::clearIfOld);
        return optional;
    }

    private Optional<CachedPlayer> getFromFile(UUID uuid) {
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        Optional<CachedPlayer> optional = cache.stream().filter(p -> p.getUUID().equals(uuid)).findFirst();
        optional.ifPresent(this::clearIfOld);
        return optional;
    }

    public CachedPlayer get(String name) {
        // From file
        Optional<CachedPlayer> fileData = getFromFile(name);
        if (fileData.isPresent()) {
            return fileData.get();
        }
        // From server cache or API
        CachedPlayer cachedPlayer = new CachedPlayer(name);
        cachedPlayer.finishIfNotCompleted();
        addToCache(cachedPlayer);
        return cachedPlayer;
    }

    public CachedPlayer get(OfflinePlayer offlinePlayer) {
        return get(offlinePlayer.getUniqueId());
    }

    public CachedPlayer get(UUID uuid) {
        // From file
        Optional<CachedPlayer> fileData = getFromFile(uuid);
        if (fileData.isPresent()) {
            return fileData.get();
        }
        // From server cache or API
        CachedPlayer cachedPlayer = new CachedPlayer(uuid);
        cachedPlayer.finishIfNotCompleted();
        addToCache(cachedPlayer);
        return cachedPlayer;
    }

    private void addToCache(CachedPlayer cachedPlayer) {
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        // Set TTL to be between 1 and 30 days. This ensures that the cache won't get invalidated at the same time thus avoiding rate limiting problems
        if (cachedPlayer.getTTL() == CachedPlayer.defaultTTL) {
            cachedPlayer.setTTL(CachedPlayer.defaultTTL+ CachedPlayer.defaultTTL * new Random().nextInt(30));
        }
        cache.add(cachedPlayer);
        playerCacheConfig.get().set("cache",cache);
        playerCacheConfig.save();
    }

    private void clearIfOld(CachedPlayer cachedPlayer) {
        if (System.currentTimeMillis()-cachedPlayer.getLastRefresh() > cachedPlayer.getTTL()) {
            // Remove if old
            ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
            cache.forEach(cps -> {
                if (cps.getUUID() == null) {
                    System.out.println("\n\nERROR ON CLEARIFOLD IN CACHEMANAGER: UUID NULL FOR "+cps.getName() + " | "+cps.getLastRefresh());
                    cache.remove(cps);
                }
            });
            Optional<CachedPlayer> optional = cache.stream().filter(cap -> cap.getUUID() != null && cap.getUUID().equals(cachedPlayer.getUUID())).findFirst();
            if (optional.isPresent()) {
                cache.remove(optional.get());
            }
            playerCacheConfig.get().set("cache",cache);
            playerCacheConfig.save();
        }
    }
}
