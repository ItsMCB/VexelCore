package me.itsmcb.vexelcore.bukkit.modules.doorman;

import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;

public class Doorman extends VexelCoreModule {

    public Doorman() {
        super.setName("Doorman");
        super.setDeveloper("ItsMCB");
        super.setPlatform(VexelCorePlatform.BUKKIT);
        super.setVersion(1.0);
        super.registerBukkitListener(new JoinEventListener());
        super.registerBukkitCommand("greet", new GreetCMD("greet"));
    }

}
