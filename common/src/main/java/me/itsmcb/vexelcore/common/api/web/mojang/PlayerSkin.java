package me.itsmcb.vexelcore.common.api.web.mojang;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PlayerSkin {

    private String value;

    private String signature;

    public PlayerSkin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public boolean hasValue() {
        return value != null && !value.isEmpty();
    }

    public boolean hasSignature() {
        return signature != null && !signature.isEmpty();
    }

    public boolean isComplete() {
        return hasValue() && hasSignature();
    }

    public URL getSkinURL() {
        try {
            String decodedValue = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(decodedValue);
            JSONObject textures = (JSONObject) jsonObject.get("textures");
            JSONObject skin = (JSONObject) textures.get("SKIN");
            return new URL((String) skin.get("url"));
        } catch (ParseException | MalformedURLException e) {
            return null;
        }
    }
}
