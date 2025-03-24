package me.itsmcb.vexelcore.bukkit;

import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import me.itsmcb.vexelcore.bukkit.api.cache.CacheManagerV2;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.DataRequestFailure;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.DataSaveFailure;
import me.itsmcb.vexelcore.bukkit.api.managers.CacheManager;
import me.itsmcb.vexelcore.bukkit.api.menu.MenuManager;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.bukkit.api.utils.PluginUtils;
import me.itsmcb.vexelcore.bukkit.plugin.PAPI;
import me.itsmcb.vexelcore.bukkit.plugin.PCMListener;
import me.itsmcb.vexelcore.bukkit.plugin.ProxyManager;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
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

    private CacheManagerV2 cacheManagerV2;
    public CacheManagerV2 getCacheManagerV2() {
        return cacheManagerV2;
    }

    private MenuManager menuManager;
    public MenuManager getMenuManager() {
        return menuManager;
    }

    private BoostedConfig mainConfig;
    public BoostedConfig getMainConfig() {
        return mainConfig;
    }

    @Override
    public void onEnable() {
        instance = this;
        // Config
        mainConfig = new BoostedConfig(getDataFolder(),"config", getResource("config.yml"), SpigotSerializer.getInstance());

        // Register plugin channel messages
        getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", new PCMListener(instance));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PCMListener(instance));

        proxyManager = new ProxyManager(instance);
        Bukkit.getPluginManager().registerEvents(proxyManager,this);
        ConfigurationSerialization.registerClass(BukkitMsgBuilder.class, "MsgBuilder");

        // Register menu system and respective listeners
        menuManager = new MenuManager(this);
        Bukkit.getPluginManager().registerEvents(menuManager, this);

        cacheManager = new CacheManager(this);
        cacheManagerV2 = new CacheManagerV2(
                this,
                mainConfig.get().getString("player-cache.host"),
                mainConfig.get().getInt("player-cache.port"),
                mainConfig.get().getString("player-cache.database"),
                mainConfig.get().getString("player-cache.user"),
                mainConfig.get().getString("player-cache.password"),
                mainConfig.get().getString("player-cache.api-keys.mcprofile"),
                mainConfig.get().getString("player-cache.api-keys.mineskin")
        );
        Bukkit.getPluginManager().registerEvents(this,this);

        // Load final things after that server has started
        getServer().getScheduler().scheduleSyncDelayedTask(this,this::loadFinalThings);

        getLogger().info("VexelCore API ${version} successfully loaded");

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
        instance.getLogger().info("PlayerCache (V1) size is "+getCacheManager().getAllFromFile().size());
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
                getCacheManager().update(e.getPlayer()); // CacheManager V1
                try {
                    getCacheManagerV2().update(e.getPlayer()); // CacheManager V2
                } catch (DataSaveFailure | DataRequestFailure ex) {
                    throw new RuntimeException(ex);
                }
                this.cancel();
            }
        }.runTaskAsynchronously(instance);
    }

}
