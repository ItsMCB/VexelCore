package me.itsmcb.vexelcore.api.modules;

public enum VexelCorePlatform {

    UNKNOWN("UNKNOWN"),
    BUKKIT("BUKKIT"),
    VELOCITY("VELOCITY");

    private String platform;

    VexelCorePlatform(String platform) {
        this.platform = platform;
    }

    public String get() {
        return platform;
    }
}
