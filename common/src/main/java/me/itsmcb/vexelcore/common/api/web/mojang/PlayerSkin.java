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

    public boolean hasValue() {
        return value != null && !value.isEmpty();
    }

    public boolean hasSignature() {
        return signature != null && !signature.isEmpty();
    }

    public boolean isComplete() {
        return hasValue() && hasSignature();
    }
}
