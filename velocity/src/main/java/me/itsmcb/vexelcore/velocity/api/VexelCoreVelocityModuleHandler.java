package me.itsmcb.vexelcore.velocity.api;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import me.itsmcb.vexelcore.api.modules.ModuleHandler;
import me.itsmcb.vexelcore.api.modules.ModuleLoadStatus;
import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.velocity.VexelCoreVelocity;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class VexelCoreVelocityModuleHandler extends ModuleHandler {

    private VexelCoreVelocity instance;

    public VexelCoreVelocityModuleHandler(VexelCoreVelocity instance, VexelCorePlatform platform) {
        super(platform);
        this.instance = instance;
    }

    @Override
    public ModuleLoadStatus enableModule(String developer, String name) {
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (module.isEmpty()) {
            return ModuleLoadStatus.NOT_FOUND;
        }
        if (module.get().getPlatform() != super.getPlatform()) {
            return ModuleLoadStatus.UNSUPPORTED_PLATFORM;
        }
        AtomicBoolean allDependenciesPresent = new AtomicBoolean(true);
        module.get().getPluginDependencies().forEach(dependency -> {
            Optional<PluginContainer> dependencyPlugin = instance.getProxyServer().getPluginManager().getPlugin(dependency);
            if (dependencyPlugin.isEmpty()) {
                allDependenciesPresent.set(false);
            }
        });
        if (!allDependenciesPresent.get()) {
            return ModuleLoadStatus.DEPENDENCY_MISSING;
        }
        // Register Velocity Listeners
        module.get().getVelocityListenerList().forEach(listener -> {
            instance.getProxyServer().getEventManager().register(instance, listener);
        });
        // Register Velocity Commands
        module.get().getVelocitySimpleCommandList().forEach((prefix, cmd) -> {
            CommandMeta meta = instance.getProxyServer().getCommandManager().metaBuilder(prefix).build();
            instance.getProxyServer().getCommandManager().register(meta, (SimpleCommand) cmd);
        });
        return ModuleLoadStatus.SUCCESS;
    }

    @Override
    public void disableModule(String developer, String name) {
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (module.isPresent()) {
            // Unregister Velocity Listeners
            module.get().getVelocityListenerList().forEach(listener -> {
                instance.getProxyServer().getEventManager().unregisterListener(instance, listener);
            });
            // Unregister Velocity Commands
            module.get().getVelocitySimpleCommandList().forEach((prefix, cmd) -> {
                instance.getProxyServer().getCommandManager().unregister(prefix);
            });
        }
    }
}
