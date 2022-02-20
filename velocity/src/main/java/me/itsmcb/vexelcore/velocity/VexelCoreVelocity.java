package me.itsmcb.vexelcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;
import me.itsmcb.vexelcore.velocity.api.VexelCoreVelocityModuleHandler;
import me.itsmcb.vexelcore.velocity.modules.doorman.Doorman;
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
    private final VexelCoreVelocityModuleHandler moduleHandler;

    public Path getDataDirectory() { return dataDirectory; }
    public ProxyServer getProxyServer() { return server; }

    @Inject
    public VexelCoreVelocity(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.instance = this;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.moduleHandler = new VexelCoreVelocityModuleHandler(instance, VexelCorePlatform.VELOCITY, dataDirectory.toFile());
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        System.out.println("VexelCore for Velocity has been initialized.");
        moduleHandler.addModule(new Doorman());
        moduleHandler.enableModule("itsmcb","doorman");

        server.getScheduler()
                .buildTask(instance, () -> {
                    moduleHandler.disableModule("itsmcb","doorman");
                    getProxyServer().sendMessage(Component.text("Doorman Velocity has been disabled!"));
                })
                .delay(15L, TimeUnit.SECONDS)
                .schedule();
    }

}
