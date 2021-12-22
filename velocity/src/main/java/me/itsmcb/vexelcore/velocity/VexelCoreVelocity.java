package me.itsmcb.vexelcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.itsmcb.logger.ProjectLogger;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.velocity.modules.VexelCoreVelocityModuleHandler;
import me.itsmcb.vexelcore.velocity.modules.doorman.DoormanVelocity;
import net.kyori.adventure.text.Component;

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
    private final ProjectLogger logger;
    private final VexelCoreVelocityModuleHandler moduleHandler;

    public Path getDataDirectory() { return dataDirectory; }
    public ProxyServer getProxyServer() { return server; }

    @Inject
    public VexelCoreVelocity(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.instance = this;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = new ProjectLogger("VexelCore", true, true);
        this.moduleHandler = new VexelCoreVelocityModuleHandler(instance, VexelCorePlatform.VELOCITY);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.log("VexelCore for Velocity has been initialized.");
        moduleHandler.addModule(new DoormanVelocity());
        moduleHandler.enableModule("itsmcb","doormanvelocity");

        server.getScheduler()
                .buildTask(instance, () -> {
                    moduleHandler.disableModule("itsmcb","doormanvelocity");
                    getProxyServer().sendMessage(Component.text("Doorman Velocity has been disabled!"));
                })
                .delay(15L, TimeUnit.SECONDS)
                .schedule();
    }

}
