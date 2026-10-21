package me.itsmcb.vexelcore.bukkit.api.framework;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Registers {@link VexelRootCommand}s with Paper's command registrar.
 *
 * <p>The registry is non-generic, allowing it to register root commands with
 * different context types. This is essential for cross-plugin composition where
 * one plugin may register its own commands and also re-register another plugin's
 * root command as a standalone alias.</p>
 */
public class VexelCommandRegistry {

    private final VexelContext context;

    public VexelCommandRegistry(@NotNull VexelContext context) {
        this.context = context;
    }

    /**
     * Registers a root command with Paper's command registrar.
     *
     * @param command The root command to register.
     */
    public void register(@NotNull VexelRootCommand<?> command) {
        this.context.getPlugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    command.build(),
                    command.getDescription(),
                    command.getAliases()
            );
        });
    }

    /**
     * Registers multiple root commands at once.
     *
     * @param commands The root commands to register.
     */
    public void registerAll(@NotNull VexelRootCommand<?>... commands) {
        for (var command : commands) {
            register(command);
        }
    }
}