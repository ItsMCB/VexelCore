package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.logger.ProjectLogger;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.bukkit.modules.VexelCoreBukkitModuleHandler;
import me.itsmcb.vexelcore.bukkit.modules.doorman.Doorman;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VexelCoreBukkit extends JavaPlugin {

    private VexelCoreBukkit instance;
    private VexelCoreBukkitModuleHandler moduleHandler;

    @Override
    public void onEnable() {
        ProjectLogger logger = new ProjectLogger("VexelCore", false, true);
        this.instance = this;
        moduleHandler = new VexelCoreBukkitModuleHandler(instance, VexelCorePlatform.BUKKIT);

        // This example will register a basic Bukkit module that contains a listener and command
        // After 20 seconds, it will automatically be unloaded.
        moduleHandler.addModule(new Doorman());
        moduleHandler.enableModule("itsmcb","doorman");
        new BukkitRunnable() {
            @Override
            public void run() {
                moduleHandler.disableModule("itsmcb","doorman");
                getServer().broadcastMessage("Doorman module has been disabled!");
            }

        }.runTaskLater(this, 20*20);
    }

    @Override
    public void onDisable() {
    }
}
