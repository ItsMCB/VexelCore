package me.itsmcb.vexelcore.velocity.modules;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import me.itsmcb.vexelcore.api.modules.ModuleHandler;
import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.velocity.VexelCoreVelocity;

import java.util.Optional;

public class VexelCoreVelocityModuleHandler extends ModuleHandler {

    private VexelCoreVelocity instance;

    public VexelCoreVelocityModuleHandler(VexelCoreVelocity instance, VexelCorePlatform platform) {
        super(platform);
        this.instance = instance;
    }

    @Override
    public void enableModule(String developer, String name) {
        Optional<VexelCoreModule> module = super.getModule(developer, name);
        if (module.isPresent()) {
            if (module.get().getPlatform().equals(super.getPlatform())) {
                // Register Velocity Listeners
                module.get().getVelocityListenerList().forEach(listener -> {
                    instance.getProxyServer().getEventManager().register(instance, listener);
                });
                // Register Velocity Commands
                module.get().getVelocitySimpleCommandList().forEach((prefix, cmd) -> {
                    CommandMeta meta = instance.getProxyServer().getCommandManager().metaBuilder(prefix).build();
                    instance.getProxyServer().getCommandManager().register(meta, (SimpleCommand) cmd);
                });
            }
        }
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
