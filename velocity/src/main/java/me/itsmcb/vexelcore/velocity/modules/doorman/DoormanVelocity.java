package me.itsmcb.vexelcore.velocity.modules.doorman;

import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.modules.VexelCorePlatform;

public class DoormanVelocity extends VexelCoreModule {

    public DoormanVelocity() {
        super.setName("doormanvelocity");
        super.setDeveloper("ItsMCB");
        super.setPlatform(VexelCorePlatform.VELOCITY);
        super.setVersion(1.0);
        super.registerVelocityListener(new JoinEventListener());
        super.registerVelocitySimpleCommand("hello", new HelloCMD());
    }
}
