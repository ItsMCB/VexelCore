package me.itsmcb.vexelcore.bukkit.api.managers;

import me.itsmcb.vexelcore.bukkit.api.config.BukkitYAMLConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

public class LocalizationManager {

    private JavaPlugin plugin;
    // TODO should probably be an enum
    private String defaultLocalizationCode;
    private HashMap<String,BukkitYAMLConfig> files = new HashMap<>();
    private String prefix;

    public LocalizationManager(JavaPlugin plugin, String defaultLocalizationCode) {
        this.plugin = plugin;
        this.defaultLocalizationCode = defaultLocalizationCode;
    }

    public boolean register(String localizationCode) {
        String fileName = "i10n"+ File.separator +localizationCode+".yml";
        InputStream inputStream = plugin.getResource(fileName);
        BukkitYAMLConfig config = new BukkitYAMLConfig(plugin.getDataFolder(),fileName, inputStream);
        if (config.exists()) {
            files.put(localizationCode, config);
        }
        return config.exists();
    }

    public String get(String path, String... placeholderReplacements) {
        String string = get(path);
        for (int i = 0; i < placeholderReplacements.length; i++) {
            string = string.replace("%"+(i+1), placeholderReplacements[i]);
        }
        return string;
    }

    public String get(String path) {
        // TODO maybe track preferred language of player to send as best language
        String string = files.get(defaultLocalizationCode).getConfig().getString(path);
        if (string == null) {
            string = "&cUnable to find language path.";
        }
        return string;
    }

    public String getWithPrefix(String path) {
        return get("prefix")+get(path);
    }

    public String getWithPrefix(String path, String... placeholderReplacements) {
        return get("prefix")+get(path, placeholderReplacements);
    }

    public void reload() {
        files.forEach((code, config) -> config.reloadConfig());
    }

}
