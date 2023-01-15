package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.bukkit.api.utils.HookUtils;
import me.itsmcb.vexelcore.bukkit.plugin.PAPI;
import me.itsmcb.vexelcore.bukkit.plugin.PCMListener;
import me.itsmcb.vexelcore.bukkit.plugin.ProxyManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class VexelCoreBukkit extends JavaPlugin {

    private static VexelCoreBukkit instance;
    private ProxyManager proxyManager;

    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Register plugin channel messages
        getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", new PCMListener(instance));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PCMListener(instance));

        proxyManager = new ProxyManager(instance);
        Bukkit.getPluginManager().registerEvents(proxyManager,this);
        ConfigurationSerialization.registerClass(BukkitMsgBuilder.class, "MsgBuilder");

        // Load final things after that server has started
        getServer().getScheduler().scheduleSyncDelayedTask(this,this::loadFinalThings);

        System.out.println("VexelCore API ${version} for Bukkit has loaded.");
    }

    private void loadFinalThings() {
        // Register PAPI placeholders
        if (HookUtils.pluginIsLoaded("PlaceholderAPI")) {
            new PAPI(this).register();
        }
    }

    public static VexelCoreBukkit getInstance() {
        return instance;
    }

}
