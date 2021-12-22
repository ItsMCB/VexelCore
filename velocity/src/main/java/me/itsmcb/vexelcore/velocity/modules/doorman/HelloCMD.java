package me.itsmcb.vexelcore.velocity.modules.doorman;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public class HelloCMD implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        invocation.source().sendMessage(Component.text("Hello there!"));
    }
}
