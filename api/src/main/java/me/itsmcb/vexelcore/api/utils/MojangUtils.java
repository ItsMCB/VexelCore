package me.itsmcb.vexelcore.api.utils;

import java.io.IOException;

public class MojangUtils {

    public static boolean uuidExists(String uuid) throws IOException {
        WebRequest webRequest = new WebRequest("https://api.mojang.com/user/profiles/" + uuid.replace("-","") + "/names");
        webRequest.setUserAgent("VexelCore/@version@");
        return webRequest.getWebRequestResponse().getResponseCode() != 204;
    }
}
