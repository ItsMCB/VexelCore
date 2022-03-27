package me.itsmcb.vexelcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.velocity.api.VexelCoreVelocityModuleHandler;
import me.itsmcb.vexelcore.velocity.commands.MainCMD;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "vexelcore",
        name = "VexelCore",
        version = "@version@",
        description = "A modular and lightweight essential features suite.",
        url = "https://github.com/ItsMCB/VexelCore",
        authors = {"ItsMCB"}
)

public class VexelCoreVelocity {

    private final VexelCoreVelocity instance;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final VexelCoreVelocityModuleHandler moduleHandler;

    public Path getDataDirectory() { return dataDirectory; }
    public ProxyServer getProxyServer() { return server; }
    public VexelCoreVelocityModuleHandler getModuleHandler() { return moduleHandler; }

    @Inject
    public VexelCoreVelocity(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.instance = this;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.moduleHandler = new VexelCoreVelocityModuleHandler(instance, VexelCorePlatform.VELOCITY, dataDirectory.toFile());
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadCommands();
        loadModules();
    }

    private void loadCommands() {
        // Main VexelCore command
        CommandMeta.Builder builder = getProxyServer().getCommandManager().metaBuilder("vexelcoreproxy");
        builder.aliases("vcp");
        getProxyServer().getCommandManager().register(builder.build(), new MainCMD(instance));
    }

    private void loadModules() {
        getProxyServer().getScheduler().buildTask(instance, () -> {
            System.out.println("Init VexelCore modules");
            moduleHandler.loadLocalModules();
        }).delay(1L, TimeUnit.SECONDS).schedule();
    }

}
