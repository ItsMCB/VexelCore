package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Abstract base class for all leaf sub-commands in the Vexel framework.
 *
 * <p>A sub-command has arguments (parsed by Brigadier) and an {@link #execute(CommandContext)}
 * method that runs the command logic. Sub-commands cannot have children — use
 * {@link VexelCommandGroup} for container nodes.</p>
 *
 * <p>Boilerplate is minimized via the convenience constructor:</p>
 * <pre>{@code
 * public class CreateCmd extends VexelSenderSubCommand<MyContext> {
 *     public CreateCmd(MyContext ctx) {
 *         super(ctx, "create", "Create a new world", "myplugin.create");
 *     }
 *
 *     @Override
 *     public @NotNull List<VexelArg<?>> getArguments() {
 *         return List.of(VexelArg.string("name", "World name"));
 *     }
 *
 *     @Override
 *     public void execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull CommandSender sender) {
 *         String name = requireArg(ctx, getArguments().get(0));
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * @param <C> The context type.
 */
public abstract class VexelSubCommand<C extends VexelContext> implements VexelCommandNode {

    protected final C context;

    private final String name;
    private final String description;
    private final String permission;

    /**
     * Convenience constructor that sets static metadata, reducing boilerplate.
     * Only {@link #getArguments()} and {@link #execute(CommandContext)} need to be overridden.
     *
     * @param context     The plugin context.
     * @param name        The literal name of this sub-command.
     * @param description The description (supports MiniMessage).
     * @param permission  The required permission.
     */
    public VexelSubCommand(@NotNull C context, @NotNull String name, @NotNull String description, @NotNull String permission) {
        this.context = context;
        this.name = name;
        this.description = description;
        this.permission = permission;
    }

    // ── Metadata (final getters — can still be overridden if dynamic behavior is needed) ──

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }

    @Override
    public @NotNull String getPermission() {
        return permission;
    }

    // ── Abstract methods ─────────────────────────────────────────────────

    /**
     * Returns the arguments for this sub-command.
     * Optional arguments must come after required arguments.
     *
     * @return A list of {@link VexelArg} instances.
     */
    @Override
    public abstract @NotNull List<VexelArg<?>> getArguments();

    /**
     * Executes the command. Called by the framework after permission checks pass.
     *
     * @param ctx The Brigadier command context.
     */
    public abstract void execute(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;

    // ── Permission / requirements ────────────────────────────────────────

    /**
     * Checks additional requirements beyond permission (e.g., must be in a certain world).
     * Used as the Brigadier {@code requires} predicate.
     *
     * @param source The command source stack.
     * @return {@code true} if the source meets all requirements.
     */
    protected boolean checkRequirements(@NotNull CommandSourceStack source) {
        return hasPermission(source);
    }

    // ── Framework execution (wraps user execute with error handling) ─────

    @Override
    public final int execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelCommandPath path) throws CommandSyntaxException {
        try {
            execute(ctx);
        } catch (CommandSyntaxException e) {
            throw e;
        } catch (Exception e) {
            context.getPlugin().getLogger().log(Level.WARNING,
                    "Unhandled exception in command '" + path.asCommand() + "': " + e.getMessage(), e);
            TextUtils.send(ctx.getSource().getSender(),
                    "<red><bold>Error:</bold> An internal error occurred. Please contact an admin.</red>");
        }
        return Command.SINGLE_SUCCESS;
    }

    // ── Brigadier building ───────────────────────────────────────────────

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> buildBrigadier(@NotNull VexelCommandPath parentPath) {
        return buildBrigadier(parentPath, getName());
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> buildBrigadier(@NotNull VexelCommandPath parentPath, @NotNull String literalName) {
        VexelCommandPath myPath = parentPath.child(literalName);

        var literal = Commands.literal(literalName)
                .requires(this::checkRequirements);

        List<VexelArg<?>> args = getArguments();

        // Validate: no required argument after an optional argument
        boolean foundOptional = false;
        for (VexelArg<?> arg : args) {
            if (arg.isOptional()) {
                foundOptional = true;
            } else if (foundOptional) {
                throw new IllegalStateException(
                        "Subcommand '" + myPath.asString() + "' has a required argument ('" + arg.getName()
                                + "') after an optional argument. Optional arguments must come last."
                );
            }
        }

        if (args.isEmpty()) {
            literal.executes(ctx -> execute(ctx, myPath));
            return literal;
        }

        // Build the node chain: [literal, arg1, arg2, ...]
        List<ArgumentBuilder<CommandSourceStack, ?>> nodes = new ArrayList<>();
        nodes.add(literal);
        for (VexelArg<?> arg : args) {
            nodes.add(arg.toBrigadier());
        }

        // Determine which nodes are executable (all remaining args must be optional)
        // k = 0 → at literal (no args provided yet)
        // k = i → at arg[i-1] (args[0..i-1] have been provided)
        for (int k = 0; k <= args.size(); k++) {
            boolean canExecute = true;
            for (int j = k; j < args.size(); j++) {
                if (!args.get(j).isOptional()) {
                    canExecute = false;
                    break;
                }
            }
            if (canExecute) {
                nodes.get(k).executes(ctx -> execute(ctx, myPath));
            } else {
                nodes.get(k).executes(ctx -> {
                    sendUsageHelp(ctx.getSource(), myPath);
                    return Command.SINGLE_SUCCESS;
                });
            }
        }

        // Link nodes in sequence: literal → arg1 → arg2 → ...
        for (int i = nodes.size() - 1; i > 0; i--) {
            nodes.get(i - 1).then(nodes.get(i));
        }

        return literal;
    }

    // ── Help ─────────────────────────────────────────────────────────────

    /**
     * Sends usage help for this sub-command, showing syntax and argument descriptions.
     *
     * @param source The command source stack.
     * @param path   The full path to this sub-command.
     */
    protected void sendUsageHelp(@NotNull CommandSourceStack source, @NotNull VexelCommandPath path) {
        String cmd = path.asCommand();

        TextUtils.send(source.getSender(),
                "<dark_gray><st>          </st> <color:#e98df5>\ud83d\udcc4 Usage: <green><cmd></green></color> <st>          </st> </dark_gray>",
                Placeholder.unparsed("cmd", cmd)
        );

        // Build syntax string with hover tooltips for argument types
        StringBuilder syntax = new StringBuilder("<yellow>").append(cmd);
        for (VexelArg<?> arg : getArguments()) {
            String dataType = arg.getTypeClass().getSimpleName();
            String argDisplay = arg.isOptional() ? " [" + arg.getName() + "]" : " <" + arg.getName() + ">";
            syntax.append(" <hover:show_text:'<aqua>")
                    .append(dataType)
                    .append("</aqua>'>")
                    .append(argDisplay)
                    .append("</hover>");
        }
        syntax.append("</yellow>");
        TextUtils.send(source.getSender(), syntax.toString());

        // Description
        TextUtils.send(source.getSender(), "<gray><desc></gray>",
                Placeholder.parsed("desc", getDescription())
        );

        // Aliases
        if (!getAliases().isEmpty()) {
            TextUtils.send(source.getSender(), "<color:#e98df5>Aliases:</color> <gray>" + String.join(", ", getAliases()) + "</gray>");
        }

        // List arguments
        if (!getArguments().isEmpty()) {
            TextUtils.send(source.getSender(), "<color:#e98df5>Arguments:</color>");
            for (VexelArg<?> arg : getArguments()) {
                String optionalTag = arg.isOptional() ? " <gray>(optional)</gray>" : "";
                TextUtils.send(source.getSender(),
                        " <aqua>- <arg></aqua>" + optionalTag + " <gray><desc></gray>",
                        Placeholder.unparsed("arg", arg.getName()),
                        Placeholder.unparsed("desc", arg.getDescription())
                );
            }
        }
    }

    // ── Type-safe argument retrieval ─────────────────────────────────────

    /**
     * Type-safe argument retrieval for required arguments.
     *
     * @param ctx The Brigadier command context.
     * @param arg The VexelArg defining the argument.
     * @param <T> The argument type.
     * @return The argument value.
     * @throws IllegalArgumentException if the argument is missing (indicates a definition bug).
     */
    protected <T> @NotNull T requireArg(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelArg<T> arg) {
        return ctx.getArgument(arg.getName(), arg.getTypeClass());
    }

    /**
     * Type-safe argument retrieval for optional arguments.
     *
     * @param ctx The Brigadier command context.
     * @param arg The VexelArg defining the argument.
     * @param <T> The argument type.
     * @return The argument value, or {@code null} if not provided.
     */
    protected <T> @Nullable T optionalArg(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelArg<T> arg) {
        try {
            return ctx.getArgument(arg.getName(), arg.getTypeClass());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ── Utilities ────────────────────────────────────────────────────────

    /**
     * Parses a raw input string into a list of arguments, respecting quotes for spaces.
     * Useful for parsing greedy string arguments manually.
     *
     * @param input The raw input string.
     * @return A list of parsed argument strings.
     */
    protected @NotNull List<String> parseQuotedArgs(@Nullable String input) {
        if (input == null || input.isBlank()) return List.of();
        StringReader reader = new StringReader(input);
        List<String> args = new ArrayList<>();
        try {
            while (reader.canRead()) {
                reader.skipWhitespace();
                if (!reader.canRead()) break;
                args.add(reader.readString());
            }
        } catch (CommandSyntaxException e) {
            return List.of(input.trim().split("\\s+"));
        }
        return args;
    }
}