package me.itsmcb.vexelcore.bukkit.api;

import me.itsmcb.vexelcore.api.modules.ModuleHandler;
import me.itsmcb.vexelcore.api.modules.ModuleLoadStatus;
import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class VexelCoreBukkitModuleHandler extends ModuleHandler {

    VexelCoreBukkit instance;
    private @NotNull CommandMap commandMap;

    public VexelCoreBukkitModuleHandler(VexelCoreBukkit instance, VexelCorePlatform platform) {
        super(platform);
        this.instance = instance;
    }

    @Override
    public ModuleLoadStatus enableModule(String developer, String name) {
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (module.isPresent()) {
            if (module.get().getPlatform().equals(super.getPlatform())) {
                // Check if dependencies are present
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
            return ModuleLoadStatus.UNSUPPORTED_PLATFORM;
        }
        return ModuleLoadStatus.NOT_FOUND;
    }

    @Override
    public void disableModule(String developer, String name) {
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (module.isPresent()) {
            // Unregister Bukkit Listeners
            module.get().getBukkitListenerList().forEach(listener -> {
                HandlerList.unregisterAll((Listener) listener);
            });
            // Unregister Bukkit Commands
            module.get().getBukkitCommandList().forEach((prefix, cmd) -> {
                Command command = (Command) cmd;
                commandMap = Bukkit.getCommandMap();
                commandMap.getKnownCommands().remove(prefix + ":" + command.getName());
                commandMap.getKnownCommands().remove(command.getName());
            });
        }
    }
}
