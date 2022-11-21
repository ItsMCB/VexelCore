package me.itsmcb.vexelcore.common.api.vendor;

public class MinecraftSoftwareVendorUtil {

    public static boolean vendorExists(String vendorName) {
        for (MinecraftSoftwareVendors vendor : MinecraftSoftwareVendors.values()) {
            if (vendor.getVendor().equals(vendorName)) {
                return true;
            }
        }
        return false;
    }

    public static MinecraftSoftwareVendors getVendor(String vendorName) {
        for (MinecraftSoftwareVendors vendor : MinecraftSoftwareVendors.values()) {
            if (vendor.getVendor().equals(vendorName)) {
                return vendor;
            }
        }
        return null;
    }

    public static MinecraftSoftwareVendor getVendorInformation(MinecraftSoftwareVendors vendor) {
        switch (vendor) {
            case PURPUR -> {
                MinecraftSoftwareVendorBuilder minecraftSoftwareVendorBuilder = new MinecraftSoftwareVendorBuilder(MinecraftSoftwareVendors.PURPUR);
                minecraftSoftwareVendorBuilder.setWebsite("https://purpurmc.org/");
                minecraftSoftwareVendorBuilder.setDiscord("https://purpurmc.org/discord");
                minecraftSoftwareVendorBuilder.setDocumentation("https://purpurmc.org/docs/");
                minecraftSoftwareVendorBuilder.setSource("https://github.com/PurpurMC/Purpur");
                minecraftSoftwareVendorBuilder.setIssues("https://github.com/PurpurMC/Purpur/issues");
                minecraftSoftwareVendorBuilder.setBuilds("https://purpurmc.org/downloads");
                return minecraftSoftwareVendorBuilder.build();
            }
        }
        return null;
    }
}
