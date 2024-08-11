package me.itsmcb.vexelcore.common.api.web.mojang;

import me.itsmcb.vexelcore.common.api.web.WebRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.UUID;

public class PlayerInformation {
    private String name;
    private UUID uuid;
    private PlayerSkin playerSkin;
    private boolean isValid = true;

    public PlayerInformation(UUID uuid) {
        this.uuid = uuid;
        getJavaPlayerSkinData(uuid);
    }

    public PlayerSkin getJavaPlayerSkinData(UUID uuid) {
        try {
            JSONParser parser = new JSONParser();
            WebRequest request = new WebRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            JSONObject skinJSON = (JSONObject) parser.parse(request.getWebRequestResponse().getWebsiteData());
            this.name = (String) skinJSON.get("name");
            JSONArray properties = (JSONArray) skinJSON.get("properties");
            this.playerSkin = new PlayerSkin((String) ((JSONObject) properties.get(0)).get("value"), (String) ((JSONObject) properties.get(0)).get("signature"));
        } catch (ParseException | IOException e) {
            System.err.println("Username error for "+uuid);
            this.isValid = false;
            return null;
        }
        return playerSkin;
    }

    public PlayerInformation(String name) {
        try {
            JSONParser parser = new JSONParser();
            WebRequest webRequest = new WebRequest("https://api.mojang.com/users/profiles/minecraft/" + name);
            JSONObject jsonObject = (JSONObject) parser.parse(webRequest.getWebRequestResponse().getWebsiteData());
            // Align name with the official format
            this.name = (String) jsonObject.get("name");
            // Save UUID
            String uuidString = (String) jsonObject.get("id");
            this.uuid = UUID.fromString(uuidString.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5"));
            // Get skin information
            getJavaPlayerSkinData(uuid);
        } catch (ParseException | IOException e) {
            this.isValid = false;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerSkin getPlayerSkin() {
        return playerSkin;
    }
}
