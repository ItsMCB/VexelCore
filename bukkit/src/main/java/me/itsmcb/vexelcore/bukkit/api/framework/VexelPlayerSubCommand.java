package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link VexelSubCommand} that can only be executed by {@link Player}s.
 *
 * <p>The framework checks that the executor is a Player before calling
 * {@link #execute(CommandContext, Player)}. Non-player senders receive an
 * error message. The command is also hidden from non-players in tab-completion
 * via the overridden {@link #hasPermission(CommandSourceStack)}.</p>
 *
 * @param <C> The context type.
 */
public abstract class VexelPlayerSubCommand<C extends VexelContext> extends VexelSubCommand<C> {

    public VexelPlayerSubCommand(@NotNull C context, @NotNull String name, @NotNull String description, @NotNull String permission) {
        super(context, name, description, permission);
    }

    @Override
    public boolean hasPermission(@NotNull CommandSourceStack source) {
        return super.hasPermission(source) && source.getExecutor() instanceof Player;
    }

    @Override
    public final void execute(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            TextUtils.send(ctx.getSource().getSender(), "<red>Only players can execute this command.</red>");
            return;
        }
        execute(ctx, player);
    }

    /**
     * Called when the command is executed by a Player.
     *
     * @param ctx    The Brigadier command context.
     * @param player The player executing the command.
     */
    public abstract void execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull Player player) throws CommandSyntaxException;
}