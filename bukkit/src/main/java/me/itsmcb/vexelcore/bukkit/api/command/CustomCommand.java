package me.itsmcb.vexelcore.bukkit.api.command;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.common.api.command.CMDHelper;
import me.itsmcb.vexelcore.common.api.utils.ArgUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CustomCommand extends Command {
    private ArrayList<CustomCommand> subCommands = new ArrayList<>();

    private ArrayList<CustomCommand> stipulatedSubCommands = new ArrayList<>();
    private HashMap<String, String> parameters = new HashMap<>();

    public CustomCommand(@NotNull String name, @NotNull String description, @NotNull String permission) {
        super(name);
        this.setDescription(description);
        this.setPermission(permission);
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        return execute(sender, this.getName(),args);
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
        // Process args to merge set of quotes into one argument
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
        executeAsAnyCommandSender(sender, newArgs);
        return true;
    }

    private boolean hasPermission(CommandSender sender) {
        boolean isBlank = Objects.equals(super.getPermission(),"");
        if (isBlank) {
            return true;
        }
        return sender.hasPermission(super.getPermission());
    }

    public void executeAsPlayer(Player player) {
        executeAsPlayer(player,new String[]{});
    }

    public void executeAsPlayer(Player player, String[] args) {
        help(player);
    }

    public void executeAsAnyCommandSender(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            executeAsPlayer(player, args);
        }
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

    public List<String> getContextCompletions(CommandSender sender, String[] args) {
        return List.of();
    }

    public void help(CommandSender sender) {
        new BukkitMsgBuilder("&8&m                                                    &r").send(sender);
        new BukkitMsgBuilder("&d📚 &7Command Help &3"+getName()+"&7:").send(sender);
        sendFormattedCommandUsage(this, sender,null);
        new BukkitMsgBuilder("&8&m                                                    &r").send(sender);
    }

    private void sendFormattedCommandUsage(CustomCommand command, CommandSender sender, String prefix) {
        String cmd = (prefix == null ? "" : prefix+" ")+command.getName();
        TextComponent cmdHelp = new BukkitMsgBuilder("&8• &d/"+cmd)
                .hover("&7Permission required: &e"+(Objects.equals(command.getPermission(), "") ? "None" : command.getPermission()))
                .clickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/"+cmd)
                .get();
        TextComponent usage = formatUsage(command.getParameters());
        TextComponent about = new BukkitMsgBuilder(" &8- &7"+command.getDescription()).get();
        sender.sendMessage(cmdHelp.append(usage).append(about));
        command.getSubCommands().forEach(scmd -> {
            sendFormattedCommandUsage(scmd,sender,cmd);
        });
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

    public TextComponent permissionError() {
        return new BukkitMsgBuilder("&cYou don't have permission to run this command!").get();
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        // Return if sender doesn't have permission
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        // Handle different argument lengths
        return switch (args.length) {
            case 0 -> Collections.emptyList(); // No arguments
            case 1 -> handleSingleArgument(getCompletions(sender), args[0]); // Single argument
            default -> handleMultipleArguments(sender, args); // Many arguments
        };
    }
    
    private List<String> handleSingleArgument(Collection<String> completions, String partialArg) {
        if (partialArg == null || partialArg.isEmpty()) {
            return new ArrayList<>(completions);
        }
        return completions.stream()
                .filter(Objects::nonNull)
                .filter(completion -> completion.toUpperCase().contains(partialArg.toUpperCase()))
                .toList();
    }

    private ArrayList<CustomCommand> getSubCommandsFromCommandList(List<CustomCommand> commandList) {
        AtomicReference<ArrayList<CustomCommand>> subCommands = new AtomicReference<>(new ArrayList<>());
        // Iterate through commands to add all subcommands
        commandList.forEach(cmd -> cmd.getSubCommands().forEach(subCmd -> {
            ArrayList<CustomCommand> temp = subCommands.get();
            temp.add(subCmd);
            subCommands.set(temp);
        }));
        return subCommands.get();
    }

    private List<String> handleMultipleArguments(CommandSender sender, String[] args) {
        List<CustomCommand> currentCommands = new ArrayList<>(List.of(this));
        List<CustomCommand> lastMatchedCommands = new ArrayList<>(List.of(this));
        // Index of previously identified subcommand
        int previousSubCommandIndex = 0; // For example, if "/p warp warpName" is typed, it is the index for "warp"

        // Identify subcommands
        for (int i = 0; i < args.length - 1; i++) {
            int finalI = i;
            List<CustomCommand> matchedCommands = currentCommands.stream()
                    .flatMap(cmd -> getSubCommandsFromCommandList(Collections.singletonList(cmd)).stream())
                    .filter(subCmd -> subCmd.getName().equalsIgnoreCase(args[finalI]))
                    .collect(Collectors.toList());
            previousSubCommandIndex = i;
            // Backwards matching for context completion
            if (matchedCommands.isEmpty()) {
                for (int j = i; j >= 0; j--) {
                    final int searchIndex = j;
                    matchedCommands = currentCommands.stream()
                            .flatMap(cmd -> getSubCommandsFromCommandList(Collections.singletonList(cmd)).stream())
                            .filter(subCmd -> subCmd.getName().equalsIgnoreCase(args[searchIndex]))
                            .collect(Collectors.toList());
                    if (!matchedCommands.isEmpty()) {
                        break;
                    }
                }
            }
            // If matches found, update last matched commands and current commands
            if (!matchedCommands.isEmpty()) {
                lastMatchedCommands = new ArrayList<>(matchedCommands);
                currentCommands = matchedCommands;
            }
        }

        // Collect completions
        List<String> completions = new ArrayList<>();
        // Add completions from subcommands, filtering by what the user has typed
        List<String> subCommandCompletions = currentCommands.stream()
                .flatMap(cmd -> cmd.getCompletions(sender).stream())
                .filter(Objects::nonNull)
                .filter(completion -> completion.toUpperCase().contains(args[args.length - 1].toUpperCase()))
                .toList();
        // Add context completions from last matched commands, filtering by what the user has typed
        List<String> contextCompletions = lastMatchedCommands.stream()
                .flatMap(cmd -> {
                    // Pass the entire original arguments to context completions
                    String[] contextArgs = Arrays.copyOfRange(args, 0, args.length);
                    return cmd.getContextCompletions(sender, contextArgs).stream();
                })
                .filter(Objects::nonNull)
                .filter(completion -> completion.toUpperCase().contains(args[args.length - 1].toUpperCase()))
                .toList();
        // Logic that ensures subcommands don't appear when they shouldn't
        int finalTest = previousSubCommandIndex;
        boolean currentCommandIsTyped = !currentCommands.stream().filter(cc -> cc.getName().equalsIgnoreCase(args[finalTest])).toList().isEmpty();
        boolean subCommandIsTyped = !subCommandCompletions.stream().filter(cc -> cc.equalsIgnoreCase(args[finalTest])).toList().isEmpty();
        if (currentCommandIsTyped || (subCommandIsTyped && currentCommands.stream().map(Command::getName).toList().contains(args[args.length-1]))) {
            completions.addAll(subCommandCompletions);
        }
        // Always add completions as their logic is handled in each subcommand override
        completions.addAll(contextCompletions);
        return completions;
    }

}
