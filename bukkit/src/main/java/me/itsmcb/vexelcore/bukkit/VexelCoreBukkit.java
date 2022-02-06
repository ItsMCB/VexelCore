package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.logger.ProjectLogger;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.bukkit.api.VexelCoreBukkitModuleHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VexelCoreBukkit extends JavaPlugin {

    private VexelCoreBukkit instance;
    private VexelCoreBukkitModuleHandler moduleHandler;

    @Override
    public void onEnable() {
        ProjectLogger logger = new ProjectLogger("VexelCore", false, true);
        this.instance = this;
        moduleHandler = new VexelCoreBukkitModuleHandler(instance, VexelCorePlatform.BUKKIT, getDataFolder());

        // Task timer will run after the server has fully loaded.
        // This allows dependencies to load first, so they can be detected by modules.

        // TODO config generate
        // TODO module onEnable and onDisable methods
        // TODO Load module jars?
        // TODO VexelCore command to manage modules.
        BukkitRunnable addModules = new BukkitRunnable() {
            @Override
            public void run() {
                // Firewall
                //moduleHandler.addModule(new Firewall());
                //moduleHandler.enableModule("itsmcb","firewall");
                moduleHandler.loadLocalModules();
            }
        };
        addModules.runTaskLater(this, 1L);
    }

    @Override
    public void onDisable() {}
}
