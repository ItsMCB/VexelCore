package me.itsmcb.vexelcore.common.api.utils;

import me.itsmcb.vexelcore.common.api.web.WebRequest;
import me.itsmcb.vexelcore.common.api.web.WebRequestResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MojangUtils {

    public static boolean uuidExists(UUID uuid) throws IOException {
        WebRequest webRequest = new WebRequest("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-","") + "/names", false);
        webRequest.setUserAgent("VexelCore/@version@");
        return webRequest.getWebRequestResponse().getResponseCode() == 200;
    }

    public static boolean usernameExists(String username) throws IOException {
        WebRequest webRequest = new WebRequest("https://api.mojang.com/user/profiles/minecraft" + username, false);
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

    public static List<String> MHFMobs() {
        return List.of(
                "MHF_Blaze",
                "MHF_CaveSpider",
                "MHF_Chicken",
                "MHF_Cow",
                "MHF_Creeper",
                "MHF_Enderman",
                "MHF_Ghast",
                "MHF_Golem",
                "MHF_LavaSlime",
                "MHF_MushroomCow",
                "MHF_Ocelot",
                "MHF_Pig",
                "MHF_PigZombie",
                "MHF_Sheep",
                "MHF_Skeleton",
                "MHF_Slime",
                "MHF_Spider",
                "MHF_Squid",
                "MHF_Villager",
                "MHF_WSkeleton",
                "MHF_Zombie");
    }

    public static List<String> MHFPlayers() {
        return  List.of("MHF_Alex","MHF_Steve","MHF_Herobrine");
    }

    public static List<String> MHFBlocks() {
        return  List.of(
                "MHF_Cactus",
                "MHF_Cake",
                "MHF_Chest",
                "MHF_CoconutB",
                "MHF_CoconutG",
                "MHF_Melon",
                "MHF_OakLog",
                "MHF_Present1",
                "MHF_Present2",
                "MHF_Pumpkin",
                "MHF_TNT",
                "MHF_TNT2");
    }

    public static List<String> MHFBonus() {
        return  List.of(
                "MHF_ArrowUp",
                "MHF_ArrowDown",
                "MHF_ArrowLeft",
                "MHF_ArrowRight",
                "MHF_Exclamation",
                "MHF_Question");
    }

    /**
     * All Marc's Head Format usernames
     *
     * @return List<String>
     */
    public static List<String> MHFHeads() {
        List<String> result = new ArrayList<>();
        result.addAll(MHFPlayers());
        result.addAll(MHFMobs());
        result.addAll(MHFBlocks());
        result.addAll(MHFBonus());
        return result;
    }
}
