package me.itsmcb.vexelcore.api.utils;

public class WebRequestResponse {

    private String websiteData;
    private int responseCode;

    public WebRequestResponse(int responseCode, String websiteData) {
        this.responseCode = responseCode;
        this.websiteData = websiteData;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public String getWebsiteData() {
        return this.websiteData;
    }
}
