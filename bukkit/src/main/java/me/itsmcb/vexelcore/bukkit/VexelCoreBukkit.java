package me.itsmcb.vexelcore.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class VexelCoreBukkit extends JavaPlugin {

    private VexelCoreBukkit instance;

    @Override
    public void onEnable() {
        this.instance = this;
        getLogger().log(Level.INFO, "VexelCore API ${version} for Bukkit has loaded.");
    }

}
