package me.itsmcb.vexelcore.velocity.modules.doorman;

import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;

public class Doorman extends VexelCoreModule {

    public Doorman() {
        super("Doorman","ItsMCB",VexelCorePlatform.VELOCITY,1.0);
        super.registerVelocityListener(new JoinEventListener());
        super.registerVelocitySimpleCommand("hello", new HelloCMD());
    }
}
