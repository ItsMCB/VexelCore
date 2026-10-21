package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Unified interface for all nodes in a Vexel command tree.
 *
 * <p>Both {@link VexelSubCommand} (leaf) and {@link VexelCommandGroup} (container)
 * implement this interface, enabling polymorphic tree building. Any node can be
 * attached as a child of any {@link VexelCommandGroup}, including
 * {@link VexelRootCommand}s from other plugins.</p>
 *
 * <p>This is the key to composability: a {@code VexelRootCommand} for Voyage
 * (registered as {@code /v}) can also be attached as a child of a LibreProperty
 * root command, making {@code /lp v create} work automatically.</p>
 */
public interface VexelCommandNode {

    /**
     * Returns the literal name of this node (used as the Brigadier literal).
     *
     * @return The node name.
     */
    @NotNull String getName();

    /**
     * Returns alternative names that redirect to this node.
     * Default is an empty list.
     *
     * @return A list of alias strings.
     */
    default @NotNull List<String> getAliases() {
        return List.of();
    }

    /**
     * Returns a human-readable description for help text.
     *
     * @return The description string (supports MiniMessage).
     */
    @NotNull String getDescription();

    /**
     * Returns the permission required to access this node.
     *
     * @return The permission string.
     */
    @NotNull String getPermission();

    /**
     * Returns the child nodes of this node.
     * For leaf nodes (subcommands), this returns an empty list.
     *
     * @return A list of child nodes.
     */
    default @NotNull List<VexelCommandNode> getChildren() {
        return List.of();
    }

    /**
     * Returns the arguments accepted by this node.
     * For container nodes (groups), this returns an empty list.
     *
     * @return A list of arguments.
     */
    default @NotNull List<VexelArg<?>> getArguments() {
        return List.of();
    }

    /**
     * Checks whether the given source has permission to access this node.
     * Subclasses can override to add additional requirements (e.g., player-only).
     *
     * @param source The command source stack.
     * @return {@code true} if the source can access this node.
     */
    default boolean hasPermission(@NotNull CommandSourceStack source) {
        return source.getSender().hasPermission(getPermission());
    }

    /**
     * Executes this node when no child matches.
     *
     * <p>For subcommands, this runs the command logic.
     * For groups, this shows paginated help.</p>
     *
     * @param ctx  The Brigadier command context.
     * @param path The full path to this node (for help/errors).
     * @return Brigadier return code (typically {@link com.mojang.brigadier.Command#SINGLE_SUCCESS}).
     */
    int execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelCommandPath path) throws CommandSyntaxException;

    /**
     * Builds the Brigadier literal argument builder for this node using its canonical name.
     *
     * <p>Called by the parent node when constructing the Brigadier tree.
     * Each node type handles its own building logic (arguments for subcommands,
     * children for groups).</p>
     *
     * @param parentPath The path from the root to this node's parent.
     * @return The Brigadier literal argument builder (un-built).
     */
    default @NotNull LiteralArgumentBuilder<CommandSourceStack> buildBrigadier(@NotNull VexelCommandPath parentPath) {
        return buildBrigadier(parentPath, getName());
    }

    /**
     * Builds the Brigadier literal argument builder for this node using a specific literal name (such as an alias).
     *
     * <p>Each node type handles its own building logic (arguments for subcommands,
     * children for groups).</p>
     *
     * @param parentPath  The path from the root to this node's parent.
     * @param literalName The literal name to use (canonical name or alias).
     * @return The Brigadier literal argument builder (un-built).
     */
    @NotNull LiteralArgumentBuilder<CommandSourceStack> buildBrigadier(@NotNull VexelCommandPath parentPath, @NotNull String literalName);
}