package me.itsmcb.vexelcore.bukkit.api.managers;

import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import me.itsmcb.vexelcore.bukkit.plugin.CachedPlayer;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class CacheManager implements Listener {
    private JavaPlugin instance;
    private BoostedConfig playerCacheConfig;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        refresh();
    }

    public CacheManager(JavaPlugin plugin) {
        this.instance = plugin;
        // Save player cache
        StandardSerializer standardSerializer = StandardSerializer.getDefault();
        standardSerializer.register(CachedPlayer.class,CachedPlayer.adapter);
        this.playerCacheConfig = new BoostedConfig(new File(plugin.getDataFolder().getParentFile()+File.separator+"VexelCore"),"player_cache",null,standardSerializer);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ArrayList<CachedPlayer> getAllFromFile() {
        return (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
    }

    private Optional<CachedPlayer> getAllFromFile(String name) {
        return getAllFromFile().stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
    }

    private Optional<CachedPlayer> getAllFromFile(UUID uuid) {
        return getAllFromFile().stream().filter(p -> p.getUUID().equals(uuid)).findFirst();
    }

    public CachedPlayer get(String name) {
        // From file
        Optional<CachedPlayer> fileData = getAllFromFile(name);
        if (fileData.isPresent()) {
            return fileData.get();
        }
        // From server cache or API
        CachedPlayer cachedPlayer = new CachedPlayer(name);
        addToCache(cachedPlayer);
        return cachedPlayer;
    }

    public CachedPlayer get(OfflinePlayer offlinePlayer) {
        return get(offlinePlayer.getUniqueId());
    }

    public CachedPlayer get(UUID uuid) {
        // From file
        Optional<CachedPlayer> fileData = getAllFromFile(uuid);
        if (fileData.isPresent()) {
            return fileData.get();
        }
        // From server cache or API
        CachedPlayer cachedPlayer = new CachedPlayer(uuid);
        addToCache(cachedPlayer);
        return cachedPlayer;
    }

    private void addToCache(CachedPlayer cachedPlayer) {
        cachedPlayer.tryToFindMissingValues();
        if (!cachedPlayer.isComplete()) {
            return;
        }
        ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
        // Set TTL to be between 1 and 30 days. This ensures that the cache won't get invalidated at the same time thus avoiding rate limiting problems
        if (cachedPlayer.getTTL() == CachedPlayer.defaultTTL) {
            cachedPlayer.setTTL(CachedPlayer.defaultTTL+ CachedPlayer.defaultTTL * new Random().nextInt(30));
        }
        cache.add(cachedPlayer);
        playerCacheConfig.get().set("cache",cache);
        playerCacheConfig.save();
    }

    public void update(Player player) {
        CachedPlayer cachedPlayer = new CachedPlayer(player.getPlayerProfile());
        clearIfOld(cachedPlayer,true);
        addToCache(cachedPlayer);
    }

    public void refresh() {
        // Slightly delay the refresh to allow for VexelCore to set the new data
        new BukkitRunnable() {
            @Override
            public void run() {
                playerCacheConfig.reload();
            }
        }.runTaskLater(instance,100);
    }

    private void clearIfOld(CachedPlayer cachedPlayer, boolean force) {
        // Temporarily not having the last cache time matter
        // System.currentTimeMillis()-cachedPlayer.getLastRefresh() > cachedPlayer.getTTL()
        if (force) {
            // Remove if old
            ArrayList<CachedPlayer> cache = (ArrayList<CachedPlayer>) playerCacheConfig.get().getList("cache");
            cache.forEach(cps -> {
                if (cps.getUUID() == null) {
                    System.out.println("\n\nERROR ON CLEARIFOLD IN CACHEMANAGER: UUID NULL FOR "+cps.getName() + " | "+cps.getLastRefresh());
                    cache.remove(cps);
                }
            });
            List<CachedPlayer> foundEntries = cache.stream().filter(cap -> cap.getUUID() != null && cap.getUUID().equals(cachedPlayer.getUUID())).toList();
            foundEntries.forEach(cache::remove);
            playerCacheConfig.get().set("cache",cache);
            playerCacheConfig.save();
        }
    }
}
