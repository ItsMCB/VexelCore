package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

/**
 * Represents the root node of a command tree, registered with Paper's command registrar.
 *
 * <p>A {@code VexelRootCommand} extends {@link VexelCommandGroup}, meaning it IS-A
 * {@link VexelCommandNode} and can be attached as a child of another root command.
 * This enables cross-plugin command composition.</p>
 *
 * <p>Supports pure subcommand trees, standalone root argument chains, and hybrid
 * root commands that have both root arguments and subcommands.</p>
 *
 * @param <C> The context type.
 */
public abstract class VexelRootCommand<C extends VexelContext> extends VexelCommandGroup<C> {

    private final boolean playerOnly;

    /**
     * Creates a root command with the given metadata.
     *
     * @param context     The plugin context.
     * @param name        The literal name (e.g., "v" for {@code /v}).
     * @param description The description for Paper's command registrar.
     * @param permission  The required permission.
     */
    public VexelRootCommand(@NotNull C context,
                            @NotNull String name,
                            @NotNull String description,
                            @NotNull String permission) {
        this(context, name, description, permission, false, null);
    }

    /**
     * Creates a root command with player-only enforcement.
     *
     * @param context     The plugin context.
     * @param name        The literal name.
     * @param description The description for Paper's command registrar.
     * @param permission  The required permission.
     * @param playerOnly  Whether only players may execute this root command.
     */
    public VexelRootCommand(@NotNull C context,
                            @NotNull String name,
                            @NotNull String description,
                            @NotNull String permission,
                            boolean playerOnly) {
        this(context, name, description, permission, playerOnly, null);
    }

    /**
     * Creates a root command with player-only enforcement and config feature gating.
     *
     * @param context      The plugin context.
     * @param name         The literal name.
     * @param description  The description for Paper's command registrar.
     * @param permission   The required permission.
     * @param playerOnly   Whether only players may execute this root command.
     * @param featureRoute Optional config route checked via {@link VexelContext#isFeatureEnabled(String)}.
     */
    public VexelRootCommand(@NotNull C context,
                            @NotNull String name,
                            @NotNull String description,
                            @NotNull String permission,
                            boolean playerOnly,
                            @Nullable String featureRoute) {
        super(context, name, description, permission, featureRoute);
        this.playerOnly = playerOnly;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of();
    }

    @Override
    public @NotNull List<VexelCommandNode> getChildren() {
        return List.of();
    }

    @Override
    public @NotNull List<VexelArg<?>> getArguments() {
        return List.of();
    }

    @Override
    protected boolean hasAutoHelp() {
        return !getChildren().isEmpty();
    }

    @Override
    public boolean hasPermission(@NotNull CommandSourceStack source) {
        if (!super.hasPermission(source)) {
            return false;
        }
        if (playerOnly && !(source.getExecutor() instanceof Player)) {
            return false;
        }
        return true;
    }

    @Override
    public int execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelCommandPath path) throws CommandSyntaxException {
        try {
            if (playerOnly) {
                if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                    TextUtils.send(ctx.getSource().getSender(), "<red>Only players can execute this command.</red>");
                    return Command.SINGLE_SUCCESS;
                }
                executeAsPlayer(ctx, player, path);
            } else {
                executeAsSender(ctx, ctx.getSource().getSender(), path);
            }
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

    protected void executeAsPlayer(@NotNull CommandContext<CommandSourceStack> ctx,
                                   @NotNull Player player,
                                   @NotNull VexelCommandPath path) throws CommandSyntaxException {
        if (!getChildren().isEmpty()) {
            sendPaginatedHelp(ctx.getSource(), path, 1);
        } else if (!getArguments().isEmpty()) {
            sendUsageHelp(ctx.getSource(), path);
        }
    }

    protected void executeAsSender(@NotNull CommandContext<CommandSourceStack> ctx,
                                   @NotNull CommandSender sender,
                                   @NotNull VexelCommandPath path) throws CommandSyntaxException {
        if (!getChildren().isEmpty()) {
            sendPaginatedHelp(ctx.getSource(), path, 1);
        } else if (!getArguments().isEmpty()) {
            sendUsageHelp(ctx.getSource(), path);
        }
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> buildBrigadier(@NotNull VexelCommandPath parentPath,
                                                                              @NotNull String literalName) {
        VexelCommandPath myPath = parentPath.child(literalName);
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal(literalName)
                .requires(this::hasPermission);

        List<VexelArg<?>> args = getArguments();
        List<VexelCommandNode> children = getChildren();

        // Validate: no required argument after an optional argument
        boolean foundOptional = false;
        for (VexelArg<?> arg : args) {
            if (arg.isOptional()) {
                foundOptional = true;
            } else if (foundOptional) {
                throw new IllegalStateException(
                        "Command '" + myPath.asString() + "' has a required argument ('" + arg.getName()
                                + "') after an optional argument. Optional arguments must come last."
                );
            }
        }

        if (args.isEmpty()) {
            literal.executes(ctx -> execute(ctx, myPath));
        } else {
            List<ArgumentBuilder<CommandSourceStack, ?>> nodes = new ArrayList<>();
            nodes.add(literal);
            for (VexelArg<?> arg : args) {
                nodes.add(arg.toBrigadier());
            }

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
                        if (!children.isEmpty()) {
                            sendPaginatedHelp(ctx.getSource(), myPath, 1);
                        } else {
                            sendUsageHelp(ctx.getSource(), myPath);
                        }
                        return Command.SINGLE_SUCCESS;
                    });
                }
            }

            for (int i = nodes.size() - 1; i > 0; i--) {
                nodes.get(i - 1).then(nodes.get(i));
            }
        }

        if (!children.isEmpty()) {
            boolean hasHelpChild = children.stream().anyMatch(c ->
                    c.getName().equals("help") || c.getAliases().contains("help"));

            if (hasAutoHelp() && !hasHelpChild) {
                var helpBuilder = Commands.literal("help")
                        .requires(this::hasPermission)
                        .executes(ctx -> {
                            sendPaginatedHelp(ctx.getSource(), myPath, 1);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    int page = ctx.getArgument("page", Integer.class);
                                    sendPaginatedHelp(ctx.getSource(), myPath, page);
                                    return Command.SINGLE_SUCCESS;
                                }));
                literal.then(helpBuilder);
            }

            for (VexelCommandNode child : children) {
                literal.then(child.buildBrigadier(myPath));

                Set<String> registered = new HashSet<>();
                registered.add(child.getName().toLowerCase(Locale.ROOT));

                for (String alias : child.getAliases()) {
                    if (registered.add(alias.toLowerCase(Locale.ROOT))) {
                        literal.then(child.buildBrigadier(myPath, alias));
                    }
                }
            }
        }

        return literal;
    }

    protected void sendUsageHelp(@NotNull CommandSourceStack source, @NotNull VexelCommandPath path) {
        String cmd = path.asCommand();

        TextUtils.send(source.getSender(),
                "<dark_gray><st>          </st> <color:#e98df5>\ud83d\udcc4 Usage: <green><cmd></green></color> <st>          </st> </dark_gray>",
                Placeholder.unparsed("cmd", cmd)
        );

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

        TextUtils.send(source.getSender(), "<gray><desc></gray>",
                Placeholder.parsed("desc", getDescription())
        );

        if (!getAliases().isEmpty()) {
            TextUtils.send(source.getSender(), "<color:#e98df5>Aliases:</color> <gray>" + String.join(", ", getAliases()) + "</gray>");
        }

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

    protected <T> @NotNull T requireArg(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelArg<T> arg) {
        return ctx.getArgument(arg.getName(), arg.getTypeClass());
    }

    protected <T> @Nullable T optionalArg(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelArg<T> arg) {
        try {
            return ctx.getArgument(arg.getName(), arg.getTypeClass());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Builds the complete Brigadier command tree for registration with Paper.
     *
     * <p>This method is called by {@link VexelCommandRegistry#register(VexelRootCommand)}.
     * It delegates to {@link #buildBrigadier(VexelCommandPath)} with an empty root path.</p>
     *
     * @return The root {@link LiteralCommandNode} for Paper registration.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> build() {
        return buildBrigadier(new VexelCommandPath()).build();
    }
}