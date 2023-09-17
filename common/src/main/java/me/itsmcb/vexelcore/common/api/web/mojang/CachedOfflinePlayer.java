package me.itsmcb.vexelcore.common.api.web.mojang;

import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import me.itsmcb.vexelcore.common.api.VexelCoreCommon;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import me.itsmcb.vexelcore.common.api.web.WebRequest;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
            if (lastRefresh-System.currentTimeMillis() > 259200000) {
                System.out.println("Refreshing CachedOfflinePlayer for "+getName() + " ("+getUuid()+")");
                this.notCached = true;
            }
        }
    }

    private void setJavaSkinFromMojangAPI() {
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

    public CachedOfflinePlayer(UUID uuid) {
        this.vexelCoreCommon = new VexelCoreCommon(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile());
        this.uuid = uuid;
        // Check if exists in cache
        setFromCache();
        // Check if Bedrock player
        if (isBedrockPlayerClean(uuid)) {
            return;
        }
        // Get from Mojang API if not already cached
        if (notCached) {
            setJavaSkinFromMojangAPI();
        }
    }

    public CachedOfflinePlayer(String name) {
        this.vexelCoreCommon = new VexelCoreCommon(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile());
        this.name = name;
        setFromCache();
        // Check if Bedrock player
        if (isBedrockPlayerClean(name)) {
            return;
        }
        // Get from Mojang API if not already cached
        if (notCached) {
            try {
                JSONParser parser = new JSONParser();
                WebRequest webRequest = new WebRequest("https://api.mojang.com/users/profiles/minecraft/" + name);
                JSONObject jsonObject = (JSONObject) parser.parse(webRequest.getWebRequestResponse().getWebsiteData());
                this.name = (String) jsonObject.get("name");
                String uuidString = (String) jsonObject.get("id");
                this.uuid = UUID.fromString(uuidString.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5"));
                setJavaSkinFromMojangAPI();
                writeToFile();
            } catch (ParseException | IOException ignored) {}
        }
    }

    private boolean isBedrockPlayerClean(UUID uuid) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            // Is a bedrock player
            if (!(api.isFloodgateId(uuid))) {
                return false;
            }
            // Is Bedrock player online?
            if (api.isFloodgatePlayer(uuid)) {
                this.setName(api.getPlayer(uuid).getJavaUsername());
            }
            this.setPlayerSkin(
                    new PlayerSkin(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxNjYwMDc4Mzc1NywKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc2MTVjYTJlMWU4ZmVlZDcxYTQ3YzQ1NWM2MGM0NjEzMjY1NTdlZWI3YzRlNTYwYjZiOGYwMDY1YTMxNzgzNGYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                            "R+6skz5tHQtnqqsSZyEAWIFSejKFcGoBynqR5SymlzLefLPwFL1JsbXJkpsAg2HR4jSvXoUl45AeyQ8rIET+D0d0S1W6zhlPqLYikl8GVuKgsUV+DuTLTWSXLq8sub/n3+HjivHjLZSN5udJdI9J4iA0QNwe/ftdutut1p5cRW65nbb0kPAedFM+VoWzICXHhPa6aFOC36pqI1ZJVThm+xhDHo0U0MUID/gA98va4xkGB2AWyUn3fxDTjA1IQ1ItDnDNJoXQv3+Duce+ZakaSjkZGReApE4Q/ygsGWRiOHquJpGS6fXAaPga2LbNX8lVXxgfkKfnu4TmnqxPwie0TZMxIPHoGPt9vnepRS/JFH3A12OqUHlLBEigtNRWQqeTlVJsX0+Gy16DVmguSPh7St3Y3zjuwUe0C3zyuBiMuqHBjYRwagQ0UhwmIZlCsYQYahUv2XroxguBaLwhnvb/WEcDaYqj23IViMUhsHbu0h02l+qIvG98OVXW8ZbY6gcFTF3a7+uFsmqKmiSkeCT9vUU9HWkhVqeRmdq1vI9Uq/FQRcW4KSSWMuSVk+8u0nM15lJWddqgtkJVVJxEoYWja1zIOmgXBxpXZHpkNCM8NHixWQt9bnsEABLyP9lwgL9zpkFDAo8WDPS1wWDAUhmpx0yh/9JrXWz5exoizATebLQ="
                    )
            );
            writeToFile();
            return true;
        } catch (Exception e) {
            System.out.println("ERROR BTW: "+e.getMessage());
            return false;
        }

    }

    private boolean isBedrockPlayerClean(String name) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            // If username is bedrock player
            if (!(api.isFloodgatePlayer(uuid))) {
                return false;
            }
            this.setUuid(api.getUuidFor(name).get());
            this.setPlayerSkin(
                    new PlayerSkin(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxNjYwMDc4Mzc1NywKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc2MTVjYTJlMWU4ZmVlZDcxYTQ3YzQ1NWM2MGM0NjEzMjY1NTdlZWI3YzRlNTYwYjZiOGYwMDY1YTMxNzgzNGYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                            "R+6skz5tHQtnqqsSZyEAWIFSejKFcGoBynqR5SymlzLefLPwFL1JsbXJkpsAg2HR4jSvXoUl45AeyQ8rIET+D0d0S1W6zhlPqLYikl8GVuKgsUV+DuTLTWSXLq8sub/n3+HjivHjLZSN5udJdI9J4iA0QNwe/ftdutut1p5cRW65nbb0kPAedFM+VoWzICXHhPa6aFOC36pqI1ZJVThm+xhDHo0U0MUID/gA98va4xkGB2AWyUn3fxDTjA1IQ1ItDnDNJoXQv3+Duce+ZakaSjkZGReApE4Q/ygsGWRiOHquJpGS6fXAaPga2LbNX8lVXxgfkKfnu4TmnqxPwie0TZMxIPHoGPt9vnepRS/JFH3A12OqUHlLBEigtNRWQqeTlVJsX0+Gy16DVmguSPh7St3Y3zjuwUe0C3zyuBiMuqHBjYRwagQ0UhwmIZlCsYQYahUv2XroxguBaLwhnvb/WEcDaYqj23IViMUhsHbu0h02l+qIvG98OVXW8ZbY6gcFTF3a7+uFsmqKmiSkeCT9vUU9HWkhVqeRmdq1vI9Uq/FQRcW4KSSWMuSVk+8u0nM15lJWddqgtkJVVJxEoYWja1zIOmgXBxpXZHpkNCM8NHixWQt9bnsEABLyP9lwgL9zpkFDAo8WDPS1wWDAUhmpx0yh/9JrXWz5exoizATebLQ="
                    )
            );
            writeToFile();
            return true;

        } catch (ExecutionException | InterruptedException e) {
            System.out.println("ERROR BTW: "+e.getMessage());
            return false;
        }
    }

    @Deprecated
    public CachedOfflinePlayer(UUID uuid, File dataFolder) {
        this.vexelCoreCommon = new VexelCoreCommon(dataFolder.getParentFile());
        this.uuid = uuid;
        // Check if exists in cache
        setFromCache();
        // Get from Mojang API if not already cached
        if (notCached) {
            setJavaSkinFromMojangAPI();
        }
    }

    @Deprecated
    public CachedOfflinePlayer(String name, File dataFolder) {
        this.vexelCoreCommon = new VexelCoreCommon(dataFolder.getParentFile());
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
                setJavaSkinFromMojangAPI();
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
        if (name == null && uuid != null) {
            return "Unknown Player ("+uuid+")";
        }
        if (name == null) {
            return "Unknown Player";
        }
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
