package me.itsmcb.vexelcore.bukkit.api.command;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.common.api.command.CMDHelper;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomCommand extends Command {
    ArrayList<CustomCommand> subCommands = new ArrayList<>();

    ArrayList<CustomCommand> stipulatedSubCommands = new ArrayList<>();
    HashMap<String, String> parameters = new HashMap<>();

    public CustomCommand(@NotNull String name, @NotNull String description, @NotNull String permission) {
        super(name);
        this.setDescription(description);
        this.setPermission(permission);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            // TODO error message from Locarzi
            if (sender instanceof Player player) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, (float) 1, 0);
            }
            sender.sendMessage(permissionError());
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

    private boolean hasPermission(CommandSender sender) {
        boolean isBlank = Objects.equals(super.getPermission(),"");
        if (isBlank) {
            return true;
        }
        return sender.hasPermission(super.getPermission());
    }

    public void executeAsPlayer(Player player, String[] args) {
        player.sendMessage(help());
    }

    public void executeAsConsole(CommandSender console, String[] args) {
        console.sendMessage(help());
    }

    public void registerSubCommand(CustomCommand subCommand) {
        subCommands.add(subCommand);
    }

    public void registerStipulatedSubCommand(CustomCommand subCommand) {
        stipulatedSubCommands.add(subCommand);
    }

    public void addParameter(String parameter, String description) {
        this.parameters.put(parameter,description);
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public List<String> getCompletions() {
        List<String> arguments = new ArrayList<>();
        subCommands.forEach(subcommand -> {
            arguments.add(subcommand.getName());
        });
        stipulatedSubCommands.forEach(subcommand -> {
            arguments.add(subcommand.getName());
        });
        arguments.addAll(getAdditionalCompletions());
        return arguments;
    }

    public List<String> getAdditionalCompletions() {
        return List.of();
    }

    public TextComponent help() {
        // TODO allow for localization
        // TODO maybe list permission if the player doesn't have it? Likely in a hover message
        // TODO argument descriptions should be a hover message over the argument
        // TODO display help for current command too (not just subcommands)
        StringBuilder sb = new StringBuilder();
        sb.append("&7===== Help - ").append(getName()).append(" =====");
        List<CustomCommand> commands = Stream.concat(subCommands.stream(), stipulatedSubCommands.stream()).toList();
        // Command
        sb.append("\n&a" + this.getName());
        this.getParameters().forEach((parameter, description) -> {
            sb.append(" " + parameter + " (" + description + ")");
        });
        sb.append(" &7- &e" + this.getDescription());
        // Subcommands
        commands.forEach(command -> {
            sb.append("\n&7> &a" + command.getName());
            command.getParameters().forEach((parameter, description) -> {
                sb.append(" " + parameter + " (" + description + ")");
            });
            sb.append(" &7- &e" + command.getDescription());
        });
        return new BukkitMsgBuilder(sb.toString()).get();
    }

    public TextComponent permissionError() {
        return new BukkitMsgBuilder("&cNo permission!").get();
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!hasPermission(sender)) {
            return List.of();
        }
        if (args.length == 1) {
            return getCompletions().stream().filter(c -> c.toUpperCase().contains(args[args.length-1].toUpperCase())).collect(Collectors.toList());
        }
        CustomCommand subCommand = subCommands.stream().filter(command -> command.getName().equalsIgnoreCase(args[args.length-2])).findFirst().orElse(null);
        if (subCommand == null) {
            return List.of();
        }
        if (!subCommand.hasPermission(sender)) {
            return List.of();
        }
        return subCommand.getCompletions().stream().filter(c -> c.toUpperCase().contains(args[args.length-1].toUpperCase())).collect(Collectors.toList());
    }
}
