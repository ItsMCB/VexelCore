package me.itsmcb.vexelcore.common.api;

import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import me.itsmcb.vexelcore.common.api.web.mojang.CachedOfflinePlayer;

import java.io.File;
import java.util.ArrayList;

public class VexelCoreCommon {

    public BoostedConfig offlinePlayerCacheConfig;
    public StandardSerializer standardSerializer;

    public BoostedConfig getOfflinePlayerCacheConfig() {
        return offlinePlayerCacheConfig;
    }

    private ArrayList<CachedOfflinePlayer> offlinePlayerCache = new ArrayList<>();

    public ArrayList<CachedOfflinePlayer> getOfflinePlayerCache() {
        // Only load from disk once
        if (this.offlinePlayerCache.isEmpty()) {
            this.offlinePlayerCache = (ArrayList<CachedOfflinePlayer>) offlinePlayerCacheConfig.get().getList("cache");
        }
        return offlinePlayerCache;
    }

    public VexelCoreCommon() {
        standardSerializer = StandardSerializer.getDefault();
        standardSerializer.register(CachedOfflinePlayer.class,CachedOfflinePlayer.adapter);
    }

    public VexelCoreCommon(File dataFolder) {
        this();
        try {
            offlinePlayerCacheConfig = new BoostedConfig(new File(dataFolder.getParentFile()+File.separator+"VexelCore"),"player_cache", null,standardSerializer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
