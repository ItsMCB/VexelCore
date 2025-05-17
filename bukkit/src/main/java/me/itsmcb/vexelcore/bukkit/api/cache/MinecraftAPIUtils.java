package me.itsmcb.vexelcore.bukkit.api.cache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.DataRequestFailure;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.PlayerNotFoundException;
import me.itsmcb.vexelcore.bukkit.api.utils.MineSkinUtil;
import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MinecraftAPIUtils {

    private static final String MOJANG_NAME = "https://api.minecraftservices.com/minecraft/profile/lookup/name/";
    private static final String MOJANG_PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String GEYSER_XUID_FROM_GAMERTAG = "https://api.geysermc.org/v2/xbox/xuid/";
    private static final String MCPROFILE_FLOODGATE_UUID = "https://mcprofile.io/api/v1/bedrock/fuid/";
    private static final String MCPROFILE_FLOODGATE_GAMERTAG = "https://mcprofile.io/api/v1/bedrock/gamertag/";
    private static final String MCPROFILE_JAVA_USERNAME = "https://mcprofile.io/api/v1/java/username/";
    private static final String MCPROFILE_JAVA_UUID = "https://mcprofile.io/api/v1/java/uuid/";

    private String mcProfileAPIKey = null;
    private String mineSkinAPIKey = null;

    public MinecraftAPIUtils() {}

    public MinecraftAPIUtils(String mcProfileAPIKey, String mineSkinAPIKey) {
        this.mcProfileAPIKey = mcProfileAPIKey;
        this.mineSkinAPIKey = mineSkinAPIKey;
    }

    /**
     * Makes an HTTP request to the given URL.
     *
     * @param urlString The URL to request
     * @return The response as a string
     * @throws PlayerNotFoundException If the player was not found (404)
     * @throws DataRequestFailure If the API fails
     */
    public static String makeHttpRequest(String urlString, HashMap<String,String> requestProperties) throws PlayerNotFoundException, DataRequestFailure {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "VexelCore Minecraft Plugin");

            // Attach relevant API keys
            requestProperties.forEach(connection::setRequestProperty);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                return response.toString();
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new PlayerNotFoundException(url);
            } else {
                if (responseCode == 429) { // Too many requests
                    Bukkit.getPluginManager().getPlugin("VexelCore").getLogger().warning("Too many requests warning received from "+urlString);
                }
                if (responseCode == 503) {
                    Bukkit.getPluginManager().getPlugin("VexelCore").getLogger().severe("This API is overloaded / experiencing 503 issues: "+urlString);
                }
                // 404 not found, etc.
                throw new DataRequestFailure("Player lookup API server sent the following HTTP error: "+responseCode);
            }
        } catch (IOException e) {
            throw new DataRequestFailure(e);
        }
    }

    /**
     * Fetches a Java player by username from the Mojang API. Falls back to MCProfile and MineSkin API.
     *
     * @param username The player's username
     * @return CachedPlayer
     * @throws PlayerNotFoundException If the player was not found (404)
     * @throws DataRequestFailure If the API fails
     */
    public CachedPlayerV2 fetchJavaPlayer(String username) throws PlayerNotFoundException, DataRequestFailure {
        // Mojang API
        try {

            String response = makeHttpRequest(MOJANG_NAME + username,new HashMap<>());
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            UUID uuid = UUID.fromString(GeyserUtils.formatUUID(json.get("id").getAsString()));
            // Then fetch complete profile with skin data
            return fetchJavaPlayer(uuid);
        } catch (DataRequestFailure ignored) { /* Ignore and move onto the fallback API */ }

        // Fallback MCProfile API
        String response = MinecraftAPIUtils.makeHttpRequest(MCPROFILE_JAVA_USERNAME + username,getMCProfileAPIData());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        String actualUsername = json.get("username").getAsString();
        String skin = json.get("skin").getAsString();

        // Setup CachedPlayer without skin data
        CachedPlayerV2 p = new CachedPlayerV2(uuid,actualUsername, CachedPlayerV2.Edition.JAVA);

        // Use MineeSkin API to obtain valid texture and signature combo from texture URL
        PlayerSkinData data = MineSkinUtil.getTextureDataFromUrl(skin,mineSkinAPIKey);
        p.setTextureData(data);
        return p;
    }


    /**
     * Fetches a Java player by UUID from the Mojang API. Falls back to MCProfile and MineSkin API.
     *
     * @param uuid The player's UUID
     * @return CachedPlayer
     * @throws PlayerNotFoundException If the player was not found (404)
     * @throws DataRequestFailure If the API fails
     */
    public CachedPlayerV2 fetchJavaPlayer(@NotNull UUID uuid) throws PlayerNotFoundException, DataRequestFailure {
        // Mojang API
        try {
            // Format UUID to remove dashes if present
            String formattedUuid = uuid.toString().replace("-", "");
            String profileResponse = MinecraftAPIUtils.makeHttpRequest(MOJANG_PROFILE_URL + formattedUuid + "?unsigned=false",new HashMap<>());

            JsonObject profileJson = JsonParser.parseString(profileResponse).getAsJsonObject();
            String username = profileJson.get("name").getAsString();
            String texture = CacheManagerV2.DEFAULT_STEVE_TEXTURE;
            String signature = CacheManagerV2.DEFAULT_STEVE_SIGNATURE;

            if (profileJson.has("properties") && !profileJson.get("properties").getAsJsonArray().isEmpty()) {
                JsonObject texturesProperty = profileJson.get("properties").getAsJsonArray().get(0).getAsJsonObject();
                texture = texturesProperty.get("value").getAsString();
                signature = texturesProperty.get("signature").getAsString();
            }
            return new CachedPlayerV2(uuid, username, CachedPlayerV2.Edition.JAVA, texture, signature);
        } catch (DataRequestFailure ignore) { /* Ignore and move onto the fallback API */ }

        // Fallback MCProfile API
        String response = makeHttpRequest(MCPROFILE_JAVA_UUID + uuid,getMCProfileAPIData());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        String actualUsername = json.get("username").getAsString();
        String skin = json.get("skin").getAsString();

        // Setup CachedPlayer without skin data
        CachedPlayerV2 p = new CachedPlayerV2(uuid,actualUsername, CachedPlayerV2.Edition.JAVA);

        // Use MineeSkin API to obtain valid texture and signature combo from texture URL
        PlayerSkinData data = MineSkinUtil.getTextureDataFromUrl(skin,mineSkinAPIKey);
        p.setTextureData(data);
        return p;
    }

    /**
     * Fetches a Bedrock player by username from the Geyser API.
     *
     * @param username The player's username (without the leading dot)
     * @return CachedPlayer
     * @throws PlayerNotFoundException If the player was not found (404)
     * @throws DataRequestFailure If the API fails
     */
    public CachedPlayerV2 fetchBedrockPlayer(String username) throws PlayerNotFoundException, DataRequestFailure {
        String usernamePrefix = FloodgateApi.getInstance().getPlayerPrefix();
        String usernameWithoutPrefix = username.substring(usernamePrefix.length());
        UUID uuid = null;

        // Try MCProfile API (preferred because it might have skin information)
        PlayerSkinData psd = new PlayerSkinData();
        try {
            HashMap<String,String> data = getMcProfile(usernameWithoutPrefix);
            if (data.containsKey("uuid")) {
                usernameWithoutPrefix = data.get("gamertag"); // Set correct casing
                uuid = UUID.fromString(data.get("uuid"));
                String skin = data.get("skin");
                if (skin != null) {
                    // MineSkin
                    try {
                        psd = MineSkinUtil.getTextureDataFromUrl(skin,mineSkinAPIKey);
                    } catch (DataRequestFailure ignore) {}
                }
                return new CachedPlayerV2(uuid,usernamePrefix+usernameWithoutPrefix, CachedPlayerV2.Edition.BEDROCK, psd.getTexture(), psd.getSignature());
            }
        } catch (PlayerNotFoundException | DataRequestFailure ignore) {}

        // Try Floodgate API
        try {
            Optional<UUID> optionalUUID = getXUIDFromFloodgateCache(usernameWithoutPrefix);
            if (optionalUUID.isPresent()) {
                uuid = optionalUUID.get();
            }
        } catch (Exception ignored) {}

        // Try Geyser API
        if (uuid == null) {
            try {
                Optional<UUID> optionalUUID = getXUIDFromGeyser(usernameWithoutPrefix);
                if (optionalUUID.isPresent()) {
                    uuid = optionalUUID.get();
                }
            } catch (Exception e) {
                throw new DataRequestFailure(e);
            }
        }
        return new CachedPlayerV2(uuid, usernamePrefix+usernameWithoutPrefix, CachedPlayerV2.Edition.BEDROCK, psd.getTexture(), psd.getSignature());
    }

    /**
     * Fetches a Bedrock player by XUID from the Geyser API.
     *
     * @param uuid The player's UUID
     * @return CachedPlayer
     * @throws PlayerNotFoundException If the player was not found (404)
     * @throws DataRequestFailure If the API fails
     */
    public CachedPlayerV2 fetchBedrockPlayer(UUID uuid)  throws PlayerNotFoundException, DataRequestFailure {
        String usernamePrefix = FloodgateApi.getInstance().getPlayerPrefix();
        String usernameWithoutPrefix = null;

        try {
            // McProfile API (preferred because it might have skin information)
            PlayerSkinData psd = new PlayerSkinData();
            HashMap<String,String> data = getMcProfile(uuid);
            if (data.containsKey("gamertag")) {
                usernameWithoutPrefix = data.get("gamertag");
                String skin = data.get("skin");
                if (skin != null) {
                    // MineSkin
                    try {
                        psd = MineSkinUtil.getTextureDataFromUrl(skin,mineSkinAPIKey);
                    } catch (DataRequestFailure ignore) {}
                }
                return new CachedPlayerV2(uuid,usernamePrefix+usernameWithoutPrefix, CachedPlayerV2.Edition.BEDROCK, psd.getTexture(), psd.getSignature());
            }

            // Floodgate API
            long xuid = GeyserUtils.getXUIDFromGeyserUUID(uuid);
            usernameWithoutPrefix = FloodgateApi.getInstance().getGamertagFor(xuid).get();

            return new CachedPlayerV2(uuid, usernamePrefix+usernameWithoutPrefix, CachedPlayerV2.Edition.BEDROCK, psd.getTexture(), psd.getSignature());
        } catch (PlayerNotFoundException | DataRequestFailure e) {
            throw new RuntimeException(e);
        } catch (ExecutionException | InterruptedException e) { // From Floodgate failure
            throw new DataRequestFailure(e);
        }
    }

    public Optional<UUID> getXUIDFromFloodgateCache(@NotNull String usernameWithoutPrefix) throws Exception {
        return Optional.ofNullable(FloodgateApi.getInstance().getUuidFor(usernameWithoutPrefix).get());
    }

    public Optional<UUID> getXUIDFromGeyser(@NotNull String usernameWithoutPrefix) throws Exception {
        String response = MinecraftAPIUtils.makeHttpRequest(GEYSER_XUID_FROM_GAMERTAG + usernameWithoutPrefix,new HashMap<>());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        long xuid = json.get("xuid").getAsLong();
        return Optional.ofNullable(GeyserUtils.getGeyserUUIDFromXUID(xuid));
    }

    public HashMap<String, String> getMCProfileAPIData() {
        HashMap<String,String> apiData = new HashMap<>();
        apiData.put("x-api-key",mcProfileAPIKey);
        return apiData;
    }

    public HashMap<String,String> getMcProfile(@NotNull UUID uuid) throws PlayerNotFoundException, DataRequestFailure {

        String response = MinecraftAPIUtils.makeHttpRequest(MCPROFILE_FLOODGATE_UUID + uuid, getMCProfileAPIData());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        HashMap<String, String> data = new HashMap<>();
        data.put("gamertag",json.get("gamertag").getAsString());
        if (!json.get("skin").isJsonNull()) {
            data.put("skin",json.get("skin").getAsString());
        }
        return data;
    }

    public HashMap<String,String> getMcProfile(@NotNull String gamerTag) throws PlayerNotFoundException, DataRequestFailure{
        HashMap<String,String> apiData = new HashMap<>();
        apiData.put("x-api-key",mcProfileAPIKey);
        String response = MinecraftAPIUtils.makeHttpRequest(MCPROFILE_FLOODGATE_GAMERTAG + gamerTag, apiData);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        HashMap<String, String> data = new HashMap<>();
        data.put("gamertag",json.get("gamertag").getAsString());
        data.put("uuid",json.get("floodgateuid").getAsString());
        if (!json.get("skin").isJsonNull()) {
            data.put("skin",json.get("skin").getAsString());
        }
        return data;
    }

}
