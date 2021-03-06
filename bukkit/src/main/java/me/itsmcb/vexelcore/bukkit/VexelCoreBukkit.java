package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.vexelcore.bukkit.plugin.PCMListener;
import me.itsmcb.vexelcore.bukkit.plugin.ProxyManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class VexelCoreBukkit extends JavaPlugin {

    private static VexelCoreBukkit instance;
    private ProxyManager proxyManager;

    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", new PCMListener(instance));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PCMListener(instance));
        proxyManager = new ProxyManager(instance);
        Bukkit.getPluginManager().registerEvents(proxyManager,this);
        getLogger().log(Level.INFO, "VexelCore API ${version} for Bukkit has loaded.");
    }

    public static VexelCoreBukkit getInstance() {
        return instance;
    }



}
