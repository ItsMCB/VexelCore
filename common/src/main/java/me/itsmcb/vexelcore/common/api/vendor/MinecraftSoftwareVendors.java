package me.itsmcb.vexelcore.common.api.vendor;

public enum MinecraftSoftwareVendors {

    PURPUR("Purpur");

    private String vendor;

    MinecraftSoftwareVendors(String vendor) {
        this.vendor = vendor;
    }

    public String getVendor() {
        return vendor;
    }

    @Override
    public String toString() {
        return String.valueOf(getVendor());
    }
}
