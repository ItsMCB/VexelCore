package me.itsmcb.vexelcore.bukkit.api.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

// Use BoostYML in the future so comments and order save correctly.
public class BukkitYAMLConfig {

    private File customConfigFile;
    private FileConfiguration customConfig;
    private File dataFolder;
    private String fileName;
    private InputStream inputStream;

    public BukkitYAMLConfig(File dataFolder, String fileName, InputStream inputStream) {
        this.dataFolder = dataFolder;
        this.fileName = fileName;
        this.inputStream = inputStream;
        saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        return this.customConfig;
    }

    private boolean saveDefaultConfig() {
        customConfigFile = new File(dataFolder, fileName);
        customConfigFile.getParentFile().mkdirs();
        // Try to create file w/no data
        try {
            customConfigFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Load data
        try {
            if (inputStream != null) {
                // Load defaults
                YamlConfiguration defaultConfig = new YamlConfiguration();
                defaultConfig.load(new InputStreamReader(new ByteArrayInputStream(inputStream.readAllBytes())));
                inputStream.close();
                // Load actual config and set defaults if needed
                reloadConfig();
                customConfig.setDefaults(defaultConfig);
                customConfig.options().copyDefaults(true);
                saveConfig();
            } else {
                System.out.println("Can't register \"" + fileName + "\" because the default plugin file doesn't exist.");
                return false;
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        reloadConfig();
        return true;
    }

    public boolean exists() {
        customConfigFile = new File(dataFolder, fileName);
        return customConfigFile.exists();
    }

    public boolean saveConfig() {
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean reloadConfig() {
        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
