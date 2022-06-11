package me.itsmcb.vexelcore.api.utils;

import me.itsmcb.vexelcore.api.web.WebRequest;
import me.itsmcb.vexelcore.api.web.WebRequestResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.UUID;

public class MojangUtils {

    public static boolean uuidExists(String uuid) throws IOException {
        WebRequest webRequest = new WebRequest("https://api.mojang.com/user/profiles/" + uuid.replace("-","") + "/names", false);
        webRequest.setUserAgent("VexelCore/@version@");
        return webRequest.getWebRequestResponse().getResponseCode() == 200;
    }

    public static String usernameFromUUID(UUID playerUUID) throws Exception {
        try {
            WebRequest webRequest = new WebRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + cleanUUID(playerUUID));
            WebRequestResponse webRequestResponse = webRequest.getWebRequestResponse();
            if (webRequestResponse.getResponseCode() == 200) {
                try {
                    JSONParser parser = new JSONParser();
                    Object resultObject = parser.parse(webRequestResponse.getWebsiteData());
                    if (resultObject instanceof JSONObject jsonObject) {
                        return jsonObject.get("name").toString();
                    }
                } catch (ParseException err) {
                    err.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception("Couldn't get username");
    }

    public static String cleanUUID(UUID uuid) {
        return uuid.toString().replace("-","");
    }
}
