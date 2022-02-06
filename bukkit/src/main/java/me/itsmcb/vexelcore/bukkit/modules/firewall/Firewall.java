package me.itsmcb.vexelcore.bukkit.modules.firewall;

import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;

public class Firewall extends VexelCoreModule {

    public Firewall() {
        super("Firewall", "ItsMCB", VexelCorePlatform.BUKKIT, 1.0);
        super.registerBukkitListener(new PlayerLoginListener());
        super.registerBukkitCommand("firewall",new FirewallCMD("firewall"));
    }

}
