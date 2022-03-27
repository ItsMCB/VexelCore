package me.itsmcb.vexelcore.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.itsmcb.vexelcore.api.utils.CMDHelper;
import me.itsmcb.vexelcore.velocity.VexelCoreVelocity;
import me.itsmcb.vexelcore.velocity.api.utils.VelocityUtils;

import java.util.List;

public class MainCMD implements SimpleCommand {

    VexelCoreVelocity instance;

    public MainCMD(VexelCoreVelocity instance) {
        this.instance = instance;
    }

    @Override
    public void execute(Invocation invocation) {
        CMDHelper cmdHelper = new CMDHelper(invocation.arguments());
        if (cmdHelper.isCalling("reload")) {
            instance.getModuleHandler().disableAllModules();
            instance.getModuleHandler().loadLocalModules();
            VelocityUtils.send(invocation.source(), "&aReload complete.");
        }
        if (cmdHelper.isCalling("fireevent")) {
            if (cmdHelper.argNotExists(1)) {
                VelocityUtils.send(invocation.source(), "&cMust provide an event to fire");
            }
            if (invocation.source() instanceof Player player) {
                switch (invocation.arguments()[1]) {
                    case "PostLoginEvent" -> instance.getProxyServer().getEventManager().fire(new PostLoginEvent(player));
                    case "ServerPostConnectEvent" -> {
                        if (cmdHelper.argNotExists(2)) {
                            VelocityUtils.send(invocation.source(), "&cMust provide a valid server name.");
                            return;
                        }
                        RegisteredServer previousServer = instance.getProxyServer().getServer(invocation.arguments()[2]).orElse(null);
                        if (previousServer == null) {
                            VelocityUtils.send(invocation.source(), "&cProvided server name doesn't exist.");
                        }
                        instance.getProxyServer().getEventManager().fire(new ServerPostConnectEvent(player, previousServer));
                    }
                }
            }
        }
        if (cmdHelper.isCalling("info")) {
            VelocityUtils.send(invocation.source(), "&7Address: &a" + instance.getProxyServer().getBoundAddress().getAddress().getCanonicalHostName());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CMDHelper cmdHelper = new CMDHelper(invocation.arguments());
        cmdHelper.addTabCompletion(cmdHelper.getMap(0, null),List.of("reload","fireevent"));
        cmdHelper.addTabCompletion(cmdHelper.getMap(1,"fireevent"), List.of("PostLoginEvent", "ServerPostConnectEvent"));
        cmdHelper.addTabCompletion(cmdHelper.getMap(2, "ServerPostConnectEvent"), instance.getProxyServer().getAllServers().stream().map(registeredServer -> registeredServer.getServerInfo().getName()).toList());
        return cmdHelper.generateTabComplete();
    }

}
