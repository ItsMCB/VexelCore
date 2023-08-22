package me.itsmcb.vexelcore.bukkit.api.command;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.common.api.command.CMDHelper;
import me.itsmcb.vexelcore.common.api.utils.ArgUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
        // Process args to merge quotes into one argument
        List<String> wipArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            StringBuilder sb = new StringBuilder();
            if (!args[i].startsWith("\"") && !args[i].endsWith("\"")) {
                wipArgs.add(args[i]);
                continue;
            }
            for (int x = i; x < args.length; x++) {
                sb.append(args[x]+"");
                if (args[x].endsWith("\"")) {
                    i = x;
                    break;
                } else {
                    sb.append(" ");
                }
            }
            // If no quote ending is found, don't skip the arg.
            wipArgs.add(sb.toString().replaceAll("\"",""));
        }
        String[] newArgs = wipArgs.toArray(new String[0]);

        CMDHelper cmdHelper = new CMDHelper(newArgs);
        // Check for sub commands
        AtomicBoolean subCommandCalled = new AtomicBoolean(false);
        subCommands.forEach(subCommand -> {
            if (cmdHelper.isCalling(subCommand.getName())) {
                subCommand.execute(sender,newArgs[0],ArgUtils.shift(newArgs));
                subCommandCalled.set(true);
            }
        });
        if (subCommandCalled.get()) {
            return true;
        }
        // Main command which might have args that are not sub commands
        if (sender instanceof Player player) {
            executeAsPlayer(player, newArgs);
        } else {
            executeAsConsole(sender, newArgs);
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
        help(player);
    }

    public void executeAsConsole(CommandSender console, String[] args) {
        help(console);
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

    public ArrayList<CustomCommand> getSubCommands() {
        ArrayList<CustomCommand> allSubCommands = new ArrayList<>(subCommands);
        allSubCommands.addAll(stipulatedSubCommands);
        return allSubCommands;
    }

    public List<String> getCompletions(CommandSender sender) {
        List<String> arguments = new ArrayList<>();
        getSubCommands().forEach(subCommand -> {
            if (subCommand.hasPermission(sender)) {
                arguments.add(subCommand.getName());
            }
        });
        arguments.addAll(getAdditionalCompletions(sender));
        return arguments;
    }

    public List<String> getAdditionalCompletions(CommandSender sender) {
        return List.of();
    }

    public void help(CommandSender sender) {
        new BukkitMsgBuilder("&8--=== &7Command Help&r&8: &a"+getName()+"&r&8 ===--").send(sender);
        ArrayList<CustomCommand> subCommands = getSubCommands();
        sendFormattedCommandUsage(this, sender, true);
        subCommands.forEach(scmd -> {
            sendFormattedCommandUsage(scmd, sender, false);
        });
    }

    private void sendFormattedCommandUsage(CustomCommand command, CommandSender sender, boolean isMain) {
        String subChar = "&7&l> ";
        TextComponent cmd = new BukkitMsgBuilder((isMain ? "" : subChar)+"&a"+command.getName())
                .hover("&7Permission required: &e"+(Objects.equals(getPermission(), "") ? "None" : getPermission()))
                .get();
        TextComponent usage = formatUsage(command.getParameters());
        TextComponent about = new BukkitMsgBuilder(" &7- &e"+command.getDescription()).get();
        sender.sendMessage(cmd.append(usage).append(about));
    }

    private TextComponent formatUsage(HashMap<String, String> input) {
        TextComponent.Builder component = Component.text();
        input.forEach((parameter, description) -> {
            component.append(new BukkitMsgBuilder(" &a"+parameter)
                    .hover("&7"+description)
                    .get());
        });
        return component.build();
    }

    @Deprecated
    public TextComponent help() {
        StringBuilder sb = new StringBuilder();
        sb.append("&8--=== &7Command Help&r&8: &a").append(getName()).append("&r&8 ===--");
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
        AtomicReference<List<String>> completions = new AtomicReference<>(List.of());
        if (args.length == 1) {
            completions.set(getCompletions(sender).stream().filter(c -> c.toUpperCase().contains(args[0].toUpperCase())).collect(Collectors.toList()));
        }
        if (args.length > 1) { // 2+
            ArrayList<CustomCommand> allSubCommands = new ArrayList<>();
            allSubCommands.add(this);
            for (int i = 0; i < args.length; i++) {
                int finalI = i;
                ArrayList<CustomCommand> fl = new ArrayList<>(allSubCommands);
                if (i > 0) { // 1+
                    fl = new ArrayList<>(allSubCommands.stream().filter(sc -> sc.getName().equalsIgnoreCase(args[finalI-1])).toList());
                }
                if (i+1 == args.length) { // Stop, get completions
                    ArrayList<String> almostFinalCompletions = new ArrayList<>();
                    for (CustomCommand customCommand : fl) {
                        almostFinalCompletions.addAll(customCommand.getCompletions(sender));
                    }
                    completions.set(almostFinalCompletions.stream().filter(c -> c.toUpperCase().contains(args[args.length-1].toUpperCase())).collect(Collectors.toList()));
                } else {
                    ArrayList<CustomCommand> subCommandsSave = new ArrayList<>(allSubCommands);
                    allSubCommands.clear();
                    allSubCommands.addAll(getSubCommandsFromCommandList(subCommandsSave));
                }
            }
        }
        return completions.get();
    }

    private ArrayList<CustomCommand> getSubCommandsFromCommandList(ArrayList<CustomCommand> customCommandsList) {
        AtomicReference<ArrayList<CustomCommand>> subCommands = new AtomicReference<>(new ArrayList<>());
        customCommandsList.forEach(cmd -> cmd.getSubCommands().forEach(subCmd -> {
            ArrayList<CustomCommand> temp = subCommands.get();
            temp.add(subCmd);
            subCommands.set(temp);
        }));
        return subCommands.get();
    }
}
