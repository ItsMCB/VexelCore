package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.vexelcore.bukkit.api.managers.CacheManager;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.bukkit.api.utils.PluginUtils;
import me.itsmcb.vexelcore.bukkit.plugin.PAPI;
import me.itsmcb.vexelcore.bukkit.plugin.PCMListener;
import me.itsmcb.vexelcore.bukkit.plugin.ProxyManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VexelCoreBukkit extends JavaPlugin implements Listener {

    private static VexelCoreBukkit instance;
    private ProxyManager proxyManager;

    public ProxyManager getProxyManager() {
        return proxyManager;
    }
    private CacheManager cacheManager;

    public CacheManager getCacheManager() {
        return cacheManager;
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

        cacheManager = new CacheManager(this);
        Bukkit.getPluginManager().registerEvents(this,this);

        // Load final things after that server has started
        getServer().getScheduler().scheduleSyncDelayedTask(this,this::loadFinalThings);


        System.out.println("VexelCore API ${version} for Bukkit has loaded.");
        // New WIP VexelCore Plugin system
        // Create data folder
        /*
        getDataFolder().mkdirs();
        File vexelCorePlugins = Path.of(getDataFolder() + File.separator + "plugins").toFile();
        vexelCorePlugins.mkdir();
        VexelCoreBukkitPluginManager pluginManager = new VexelCoreBukkitPluginManager(vexelCorePlugins);
        pluginManager.loadPluginsFromFolder();
        pluginManager.enablePlugins();

         */
    }

    private void loadFinalThings() {
        // Register PAPI placeholders
        if (PluginUtils.pluginIsLoaded("PlaceholderAPI")) {
            new PAPI(this).register();
        }
        // Clean player cache
        getCacheManager().cleanInvalid();
        instance.getLogger().info("PlayerCache size is "+getCacheManager().getAllFromFile().size());
    }

    public static VexelCoreBukkit getInstance() {
        return instance;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        // Run async because it will lock the server up otherwise if there are rate limit problems
        new BukkitRunnable() {
            @Override
            public void run() {
                getCacheManager().update(e.getPlayer());
                this.cancel();
            }
        }.runTaskAsynchronously(instance);
    }

}
