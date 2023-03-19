package me.itsmcb.vexelcore.common.api.web.mojang;

import me.itsmcb.vexelcore.common.api.web.WebRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class OnlinePlayerSkin {

    protected OnlinePlayerSkin() {}

    private String playerName;
    private String playerStrippedUUID;
    private PlayerSkin playerSkin;
    private boolean informationComplete = false;

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerStrippedUUID() {
        return playerStrippedUUID;
    }

    public PlayerSkin getPlayerSkin() {
        return playerSkin;
    }

    public boolean isInformationComplete() {
        return informationComplete;
    }

    public OnlinePlayerSkin(String playerName) {
        this.playerName = playerName;
        setInformation();
    }

    private void setInformation() {
        try {
            JSONParser parser = new JSONParser();
            WebRequest webRequest = new WebRequest("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            JSONObject jsonObject = (JSONObject) parser.parse(webRequest.getWebRequestResponse().getWebsiteData());
            this.playerName = (String) jsonObject.get("name");
            this.playerStrippedUUID = (String) jsonObject.get("id");
            if (playerStrippedUUID != null) {
                // Get skin info
                WebRequest skinWR = new WebRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + this.playerStrippedUUID + "?unsigned=false");
                JSONObject skinJSON = (JSONObject) parser.parse(skinWR.getWebRequestResponse().getWebsiteData());

                JSONArray properties = (JSONArray) skinJSON.get("properties");
                this.playerSkin = new PlayerSkin((String) ((JSONObject) properties.get(0)).get("value"), (String) ((JSONObject) properties.get(0)).get("signature"));
                this.informationComplete = true;
            }
        } catch (ParseException | IOException e) {
            // Ignore error
        }
    }


}
