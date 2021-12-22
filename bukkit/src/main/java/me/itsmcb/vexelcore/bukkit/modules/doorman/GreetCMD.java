package me.itsmcb.vexelcore.bukkit.modules.doorman;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GreetCMD extends @NotNull Command {

    public GreetCMD(@NotNull String name) {
        super(name);
    }

    protected GreetCMD(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        sender.sendMessage("Hello there, " + sender.getName() + "!");
        return false;
    }
}
