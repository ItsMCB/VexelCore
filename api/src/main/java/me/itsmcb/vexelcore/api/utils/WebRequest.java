package me.itsmcb.vexelcore.api.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WebRequest {
    private URL url;
    private String userAgent = null;
    private String requestMethod = "GET";
    private boolean ignoreResponseCode = false;

    public WebRequest(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    public WebRequest(String url, boolean ignoreResponseCode) throws MalformedURLException {
        this.url = new URL(url);
        this.ignoreResponseCode = ignoreResponseCode;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    private String getWebsiteData() {
        String data = "";
        try {
            Scanner scanner = new Scanner(url.openStream());

            while (scanner.hasNext()) {
                data += scanner.nextLine();
            }
            scanner.close();
        } catch (IOException e) {
            if (!ignoreResponseCode) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public WebRequestResponse getWebRequestResponse() throws IOException {
        HttpURLConnection connection = (HttpURLConnection)this.url.openConnection();
        connection.setRequestMethod(requestMethod);
        if (userAgent != null) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
        return new WebRequestResponse(connection.getResponseCode(),getWebsiteData());
    }
}
