package me.itsmcb.vexelcore.common.api.mcsoftwarevendor;

public class MinecraftSoftwareVendor {

    private MinecraftSoftwareVendors vendor;
    private String website;
    private String documentation;
    private String discord;
    private String source;
    private String issues;
    private String builds;

    public MinecraftSoftwareVendor(MinecraftSoftwareVendors vendor, String website, String documentation, String builds, String discord, String source, String issues) {
        this.vendor = vendor;
        this.website = website;
        this.documentation = documentation;
        this.builds = builds;
        this.discord = discord;
        this.source = source;
        this.issues = issues;
    }

    public MinecraftSoftwareVendors getVendor() {
        return vendor;
    }

    public String getWebsite() {
        return website;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getBuilds() {
        return builds;
    }

    public String getDiscord() {
        return discord;
    }

    public String getSource() {
        return source;
    }

    public String getIssues() {
        return issues;
    }
}
