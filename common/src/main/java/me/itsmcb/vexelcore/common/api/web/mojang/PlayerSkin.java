package me.itsmcb.vexelcore.common.api.web.mojang;

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
}
