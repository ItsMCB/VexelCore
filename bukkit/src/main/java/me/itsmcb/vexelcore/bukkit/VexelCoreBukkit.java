package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.bukkit.api.VexelCoreBukkitModuleHandler;
import me.itsmcb.vexelcore.bukkit.commands.MainCMD;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VexelCoreBukkit extends JavaPlugin {

    private VexelCoreBukkit instance;
    private VexelCoreBukkitModuleHandler moduleHandler;

    public VexelCoreBukkitModuleHandler getModuleHandler() {
        return moduleHandler;
    }

    @Override
    public void onEnable() {
        this.instance = this;
        moduleHandler = new VexelCoreBukkitModuleHandler(instance, VexelCorePlatform.BUKKIT, getDataFolder());
        loadCommands();
        loadModules();
        // TODO config generation
        // TODO module onEnable and onDisable methods
        // TODO VexelCore command to manage modules.
    }

    @Override
    public void onDisable() {}

    /**
     * Loads VexelCore modules once server has finished starting.
     */
    private void loadModules() {
        BukkitRunnable addModules = new BukkitRunnable() {
            @Override
            public void run() {
                moduleHandler.loadLocalModules();
            }
        };
        addModules.runTaskLater(this, 1L);
    }

    private void loadCommands() {
        getCommand("vexelcore").setExecutor(new MainCMD(instance));
    }
}
