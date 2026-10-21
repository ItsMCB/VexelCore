package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link VexelSubCommand} that can be executed by any {@link CommandSender}
 * (Players and Console).
 *
 * <p>The framework extracts the {@link CommandSender} from the context and passes
 * it to {@link #execute(CommandContext, CommandSender)}.</p>
 *
 * @param <C> The context type.
 */
public abstract class VexelSenderSubCommand<C extends VexelContext> extends VexelSubCommand<C> {

    public VexelSenderSubCommand(@NotNull C context, @NotNull String name, @NotNull String description, @NotNull String permission) {
        super(context, name, description, permission);
    }

    @Override
    public final void execute(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        execute(ctx, ctx.getSource().getSender());
    }

    /**
     * Called when the command is executed by a valid sender.
     *
     * @param ctx    The Brigadier command context.
     * @param sender The command sender (Player or Console).
     */
    public abstract void execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull CommandSender sender) throws CommandSyntaxException;
}