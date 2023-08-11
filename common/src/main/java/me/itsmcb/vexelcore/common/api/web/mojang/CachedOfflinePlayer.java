package me.itsmcb.vexelcore.common.api.web.mojang;

import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import me.itsmcb.vexelcore.common.api.VexelCoreCommon;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import me.itsmcb.vexelcore.common.api.web.WebRequest;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CachedOfflinePlayer {
    private UUID uuid;
    private String name;
    private long lastRefresh = System.currentTimeMillis();
    private PlayerSkin playerSkin;

    // Other
    private VexelCoreCommon vexelCoreCommon;
    private boolean notCached = true;

    public CachedOfflinePlayer() {}

    private void setFromCache() {
        Optional<CachedOfflinePlayer> cachedOfflinePlayerOptional = vexelCoreCommon.getOfflinePlayerCache().stream().filter(item -> (this.uuid == null) ? item.getName().equalsIgnoreCase(this.getName()) : item.getUuid().toString().equalsIgnoreCase(this.uuid.toString())).findFirst();
        if (cachedOfflinePlayerOptional.isPresent()) {
            CachedOfflinePlayer cachedOfflinePlayer = cachedOfflinePlayerOptional.get();
            this.setName(cachedOfflinePlayer.getName());
            this.setUuid(cachedOfflinePlayer.getUuid());
            this.setLastRefresh(cachedOfflinePlayer.getLastRefresh());
            this.setPlayerSkin(cachedOfflinePlayer.getPlayerSkin());
            this.notCached = false;
            // If it has been 3 days, refresh cache.
            if (System.currentTimeMillis() > this.lastRefresh+259200000) {
                this.notCached = true;
            }
        }
    }

    private void setSkin() {
        try {
            JSONParser parser = new JSONParser();
            WebRequest skinWR = new WebRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            JSONObject skinJSON = (JSONObject) parser.parse(skinWR.getWebRequestResponse().getWebsiteData());
            JSONArray properties = (JSONArray) skinJSON.get("properties");
            this.playerSkin = new PlayerSkin((String) ((JSONObject) properties.get(0)).get("value"), (String) ((JSONObject) properties.get(0)).get("signature"));
            this.name = (String) skinJSON.get("name");
            writeToFile();
        } catch (ParseException | IOException e) {
            // Ignore error
        }
    }

    public CachedOfflinePlayer(UUID uuid, File dataFolder) {
        this.vexelCoreCommon = new VexelCoreCommon(dataFolder);
        this.uuid = uuid;
        // Check if exists in cache
        setFromCache();
        // Get from Mojang API if not already cached
        if (notCached) {
            setSkin();
        }

    }

    public CachedOfflinePlayer(String name, File dataFolder) {
        this.vexelCoreCommon = new VexelCoreCommon(dataFolder);
        this.name = name;
        setFromCache();
        // Get from Mojang API if not already cached
        if (notCached) {
            try {
                JSONParser parser = new JSONParser();
                WebRequest webRequest = new WebRequest("https://api.mojang.com/users/profiles/minecraft/" + name);
                JSONObject jsonObject = (JSONObject) parser.parse(webRequest.getWebRequestResponse().getWebsiteData());
                this.name = (String) jsonObject.get("name");
                String uuidString = (String) jsonObject.get("id");
                this.uuid = UUID.fromString(uuidString.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5"));
                setSkin();
                writeToFile();
            } catch (ParseException | IOException e) {
                // ignore
            }
        }

    }

    private void writeToFile() {
        // Write to file if not already cached
        if (notCached) {
            BoostedConfig config = vexelCoreCommon.getOfflinePlayerCacheConfig();
            ArrayList<CachedOfflinePlayer> cache = (ArrayList<CachedOfflinePlayer>) config.get().getList("cache");
            // Remove if already exists
            Optional<CachedOfflinePlayer> existingCacheOptional = cache.stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst();
            existingCacheOptional.ifPresent(cache::remove);
            // Add to file cache
            cache.add(this);
            config.get().set("cache",cache);
            config.save();
            // Add to current process cache
            vexelCoreCommon.getOfflinePlayerCache().add(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(long lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public PlayerSkin getPlayerSkin() {
        return playerSkin;
    }

    public void setPlayerSkin(PlayerSkin playerSkin) {
        this.playerSkin = playerSkin;
    }

    public boolean hasPlayerSkin() {
        return playerSkin != null;
    }

    public static TypeAdapter<CachedOfflinePlayer> adapter = new TypeAdapter<>() {

        @NotNull
        @Override
        public Map<Object, Object> serialize(@NotNull CachedOfflinePlayer object) {
            LinkedHashMap<Object, Object> lhm = new LinkedHashMap<>();
            lhm.put("uuid", object.getUuid().toString());
            lhm.put("name", object.getName());
            lhm.put("last", object.getLastRefresh());
            if (object.hasPlayerSkin()) {
                lhm.put("value", object.getPlayerSkin().getValue());
                lhm.put("signature", object.getPlayerSkin().getSignature());
            }
            return lhm;
        }

        @NotNull
        @Override
        public CachedOfflinePlayer deserialize(@NotNull Map<Object, Object> map) {
            CachedOfflinePlayer cachedOfflinePlayer = new CachedOfflinePlayer();
            cachedOfflinePlayer.setName((String) map.get("name"));
            cachedOfflinePlayer.setUuid(UUID.fromString((String) map.get("uuid")));
            cachedOfflinePlayer.setLastRefresh((long) map.get("last"));
            if (map.containsKey("value")) {
                cachedOfflinePlayer.setPlayerSkin(new PlayerSkin(
                        (String) map.get("value"),
                        (String) map.get("signature")
                ));
            }
            return cachedOfflinePlayer;
        }
    };
}
