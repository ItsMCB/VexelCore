package me.itsmcb.vexelcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;

@Plugin(
        id = "vexelcore",
        name = "VexelCore",
        version = "${version}",
        description = "API",
        url = "https://github.com/ItsMCB/VexelCore",
        authors = {"ItsMCB"}
)

public class VexelCoreVelocity {

    private final VexelCoreVelocity instance;
    private final ProxyServer server;
    private final Path dataDirectory;

    public Path getDataDirectory() { return dataDirectory; }
    public ProxyServer getProxyServer() { return server; }

    @Inject
    public VexelCoreVelocity(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.instance = this;
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        System.out.println("VexelCore API ${version} for Velocity has loaded.");
    }

}
