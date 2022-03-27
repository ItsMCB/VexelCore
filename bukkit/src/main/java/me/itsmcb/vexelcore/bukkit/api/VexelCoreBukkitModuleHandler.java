package me.itsmcb.vexelcore.bukkit.api;

import me.itsmcb.vexelcore.api.modules.ModuleHandler;
import me.itsmcb.vexelcore.api.modules.ModuleLoadStatus;
import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class VexelCoreBukkitModuleHandler extends ModuleHandler {

    VexelCoreBukkit instance;

    public VexelCoreBukkitModuleHandler(VexelCoreBukkit instance, VexelCorePlatform platform, File dataFolder) {
        super(platform, dataFolder);
        this.instance = instance;
    }

    @Override
    public ModuleLoadStatus enableModule(String developer, String name) {
        System.out.println("Attempting to enable module " + name + " by " + developer);
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (!module.isPresent()) {
            return ModuleLoadStatus.NOT_FOUND;
        }
        if (module.get().getPlatform() != super.getPlatform()) {
            return ModuleLoadStatus.UNSUPPORTED_PLATFORM;
        }
        AtomicBoolean allDependenciesPresent = new AtomicBoolean(true);
        module.get().getPluginDependencies().forEach(dependency -> {
            Plugin dependencyPlugin = Bukkit.getPluginManager().getPlugin(dependency);
            if (dependencyPlugin != null) {
                if (!dependencyPlugin.isEnabled()) {
                    allDependenciesPresent.set(false);
                }
            } else {
                allDependenciesPresent.set(false);
            }
        });
        if (!allDependenciesPresent.get()) {
            return ModuleLoadStatus.DEPENDENCY_MISSING;
        }
        // Register Bukkit Listeners
        module.get().getBukkitListenerList().forEach(listener -> {
            Bukkit.getPluginManager().registerEvents((Listener) listener, instance);
        });
        // Register Bukkit Commands
        module.get().getBukkitCommandList().forEach((prefix, cmd) -> {
            Bukkit.getCommandMap().register(prefix, (Command) cmd);
            CraftServer server = (CraftServer) Bukkit.getServer();
            server.syncCommands();
        });
        return ModuleLoadStatus.SUCCESS;
    }

    @Override
    public void unloadModule(String developer, String name) {
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (module.isPresent()) {
            // Unregister Bukkit Listeners
            module.get().getBukkitListenerList().forEach(listener -> {
                HandlerList.unregisterAll((Listener) listener);
            });
            // Unregister Bukkit Commands
            module.get().getBukkitCommandList().forEach((prefix, cmd) -> {
                Command command = (Command) cmd;
                Bukkit.getCommandMap().getKnownCommands().remove(command.getName());
            });
            CraftServer server = (CraftServer) Bukkit.getServer();
            server.syncCommands();
            // Remove old module data from cache
            super.removeModule(module.get());
            // Log unload
            System.out.println("Disabled " + name + " by " + developer);
        }
    }

    @Override
    public void disableAllModules() {
        for (VexelCoreModule module : super.getModuleList().stream().toList() ) {
            unloadModule(module.getDeveloper(), module.getName());
        }
    }
}
