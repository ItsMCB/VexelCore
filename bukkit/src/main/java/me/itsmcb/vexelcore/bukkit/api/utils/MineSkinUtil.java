package me.itsmcb.vexelcore.bukkit.api.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.itsmcb.vexelcore.bukkit.api.cache.PlayerSkinData;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.DataRequestFailure;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MineSkinUtil {
    /**
     * Generates texture and signature data from a skin URL using the MineSkin API
     *
     * @param skinUrl URL to the Minecraft skin texture
     * @return Map containing "value" (texture) and "signature" keys
     * @throws DataRequestFailure If an error is returned
     */
    public static PlayerSkinData getTextureDataFromUrl(@NotNull String skinUrl, String apiKey) throws DataRequestFailure {
        try {
            // MineSkin API endpoint for URL-based uploads
            HttpURLConnection connection = prepareMineSkinConnection(skinUrl, apiKey);

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new DataRequestFailure("MineSkin API error: "+responseCode);
            }

            // Read the response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject data = jsonResponse.getAsJsonObject("data");
                JsonObject texture = data.getAsJsonObject("texture");

                return new PlayerSkinData(texture.get("value").getAsString(),texture.get("signature").getAsString());
            }
        } catch (IOException e) {
            throw new DataRequestFailure(e);
        }

    }

    private static HttpURLConnection prepareMineSkinConnection(@NotNull String skinUrl, String apiKey) throws IOException {
        HttpURLConnection connection = configureMineSkinConnection(apiKey);

        // Create JSON payload
        String jsonInputString = String.format("{\"url\": \"%s\", \"visibility\": 1}", skinUrl);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    private static HttpURLConnection configureMineSkinConnection(String apiKey) throws IOException {
        URL url = new URL("https://api.mineskin.org/generate/url");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configure connection for POST request
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        if (apiKey != null) {
            connection.setRequestProperty("Authorization", "Bearer "+ apiKey);
        }
        connection.setDoOutput(true);
        return connection;
    }
}
