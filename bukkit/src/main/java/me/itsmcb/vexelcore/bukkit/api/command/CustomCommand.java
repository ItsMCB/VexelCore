package me.itsmcb.vexelcore.bukkit.api.command;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.common.api.command.CMDHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomCommand extends Command {

    String permission = "";
    ArrayList<CustomCommand> subCommands = new ArrayList<>();

    public CustomCommand(@NotNull String name, @NotNull String description, String permission) {
        super(name);
        this.setDescription(description);
        this.setPermission(permission);
    }

    public void registerSubCommand(CustomCommand subCommand) {
        subCommands.add(subCommand);
    }
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!Objects.equals(permission, "") && !sender.hasPermission(permission)) {
            // TODO error message from Locarzi
            new BukkitMsgBuilder("&cNo permission!").send(sender);
            return true;
        }
        CMDHelper cmdHelper = new CMDHelper(args);
        // Check for sub commands
        AtomicBoolean subCommandCalled = new AtomicBoolean(false);
        subCommands.forEach(subCommand -> {
            if (cmdHelper.isCalling(subCommand.getName())) {
                subCommand.execute(sender,args[0],Arrays.copyOfRange(args,1,args.length));
                subCommandCalled.set(true);
            }
        });
        if (subCommandCalled.get()) {
            return true;
        }
        // Main command which might have args that are not sub commands
        if (sender instanceof Player player) {
            executeAsPlayer(player, args);
        } else {
            executeAsConsole(sender, args);
        }
        return true;
    }

    public void executeAsPlayer(Player player, String[] args) {
        // To be overridden
    }

    public void executeAsConsole(CommandSender console, String[] args) {
        // To be overridden
    }

    public List<String> getSubCommandNames() {
        List<String> arguments = new ArrayList<>();
        subCommands.forEach(subCommand -> arguments.addAll(Collections.singleton(subCommand.getName())));
        return arguments;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        // TODO allow for "options" too for situations like Voyage's world creator passing a "--flag"
        return getSubCommandNames();
    }
}
