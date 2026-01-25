package me.itsmcb.vexelcore.bukkit;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

public class VexelCoreBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        // Empty intentionally. Allows other Paper plugins to depend on VexelCore during their bootstrap process (iirc).
    }
}
