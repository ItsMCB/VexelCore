package me.itsmcb.vexelcore.bukkit.api.config;

import java.io.*;
import java.util.Properties;

public class ServerProperties {

    // Inspired by https://bukkit.org/threads/server-properties.3710/#post-47772

    public static String getString(File file, String key) {
        Properties pr = new Properties();
        try {
            FileInputStream in = new FileInputStream(file);
            pr.load(in);
            return pr.getProperty(key);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setString(File file, String key, String value) {
        Properties pr = new Properties();
        try {
            FileInputStream in = new FileInputStream(file);
            pr.load(in);
            pr.setProperty(key,value);
            pr.store(new FileOutputStream(file), null);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
