package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the root node of a command tree, registered with Paper's command registrar.
 *
 * <p>A {@code VexelRootCommand} extends {@link VexelCommandGroup}, meaning it IS-A
 * {@link VexelCommandNode} and can be attached as a child of another root command.
 * This enables cross-plugin command composition.</p>
 *
 * <p>Example: Voyage's root command registered standalone as {@code /v} and also
 * attached to LibreProperty's root as {@code /lp v}:</p>
 * <pre>{@code
 * // In Voyage plugin:
 * VexelRootCommand<VoyageContext> voyageRoot = new VoyageCommand(voyageCtx);
 * voyageRegistry.register(voyageRoot);  // /v create, /v delete, etc.
 *
 * // In LibreProperty plugin:
 * VexelRootCommand<LPContext> lpRoot = new LPCommand(lpCtx, voyageRoot);
 * lpRegistry.register(lpRoot);  // /lp create, /lp v create, etc.
 * }</pre>
 *
 * @param <C> The context type.
 */
public abstract class VexelRootCommand<C extends VexelContext> extends VexelCommandGroup<C> {

    /**
     * Creates a root command with the given metadata.
     *
     * @param context     The plugin context.
     * @param name        The literal name (e.g., "v" for {@code /v}).
     * @param description The description for Paper's command registrar.
     * @param permission  The required permission.
     */
    public VexelRootCommand(@NotNull C context, @NotNull String name, @NotNull String description, @NotNull String permission) {
        super(context, name, description, permission);
    }

    /**
     * Returns aliases for the root command (registered with Paper).
     * Default is an empty list.
     *
     * @return A list of alias strings.
     */
    @Override
    public @NotNull List<String> getAliases() {
        return List.of();
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