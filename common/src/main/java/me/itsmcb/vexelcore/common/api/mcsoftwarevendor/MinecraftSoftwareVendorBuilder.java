package me.itsmcb.vexelcore.common.api.mcsoftwarevendor;

public class MinecraftSoftwareVendorBuilder {

    private final MinecraftSoftwareVendors vendor;
    private String website = null;
    private String documentation = null;
    private String discord = null;
    private String source = null;
    private String issues = null;
    private String builds = null;

    public MinecraftSoftwareVendorBuilder(MinecraftSoftwareVendors vendor) {
        this.vendor = vendor;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public void setDiscord(String discord) {
        this.discord = discord;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public void setBuilds(String builds) {
        this.builds = builds;
    }

    public MinecraftSoftwareVendor build() {
        return new MinecraftSoftwareVendor(vendor, website, documentation, builds, discord, source, issues);
    }
}
