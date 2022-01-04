package me.itsmcb.vexelcore.velocity.modules.doorman;

import com.velocitypowered.api.command.SimpleCommand;
import me.itsmcb.vexelcore.api.utils.Icon;
import me.itsmcb.vexelcore.velocity.utils.VelocityUtils;
import net.kyori.adventure.text.Component;

public class HelloCMD implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        VelocityUtils.sendMsg(invocation.source(), "&7Hello there! &c" + Icon.HEART, "&7Have a nice day &e" + Icon.SMILEY_FACE);
    }
}
