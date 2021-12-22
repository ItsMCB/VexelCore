package me.itsmcb.vexelcore.bukkit.modules;

import me.itsmcb.vexelcore.api.modules.ModuleHandler;
import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VexelCoreBukkitModuleHandler extends ModuleHandler {

    VexelCoreBukkit instance;
    private @NotNull CommandMap commandMap;

    public VexelCoreBukkitModuleHandler(VexelCoreBukkit instance, VexelCorePlatform platform) {
        super(platform);
        this.instance = instance;
    }

    @Override
    public void enableModule(String developer, String name) {
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (module.isPresent()) {
            if (module.get().getPlatform().equals(super.getPlatform())) {
                // Register Bukkit Listeners
                module.get().getBukkitListenerList().forEach(listener -> {
                    Bukkit.getPluginManager().registerEvents((Listener) listener, instance);
                });
                // Register Bukkit Commands
                module.get().getBukkitCommandList().forEach((prefix, cmd) -> {
                    Bukkit.getCommandMap().register(prefix, (Command) cmd);
                });
            }
        }
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
