package me.itsmcb.vexelcore.bukkit.api.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BukkitYAMLConfig {

    private File customConfigFile;
    private FileConfiguration customConfig;
    private File dataFolder;
    private String fileName;
    private InputStream inputStream;
    private File targetFile;

    public BukkitYAMLConfig(File dataFolder, String fileName, InputStream inputStream) {
        this.dataFolder = dataFolder;
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.targetFile = new File(dataFolder+File.separator+fileName);
        saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        return this.customConfig;
    }

    private boolean saveDefaultConfig() {
        customConfigFile = new File(dataFolder, fileName);
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            try (FileOutputStream outputStream = new FileOutputStream(targetFile, false)) {
                int read;
                byte[] bytes = new byte[8192];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        reloadConfig();
        return true;
    }

    public boolean saveConfig() {
        try {
            customConfig.save(targetFile);
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
