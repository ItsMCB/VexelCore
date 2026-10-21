package me.itsmcb.vexelcore.bukkit.api.framework;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Core context interface for the Vexel framework.
 * Provides service location and plugin instance access.
 */
public interface VexelContext {

    /**
     * Retrieves a registered service.
     *
     * @param clazz The class of the service to retrieve.
     * @param <T>   The type of the service.
     * @return The service instance, or {@code null} if not registered.
     */
    <T> @Nullable T getService(@NotNull Class<T> clazz);

    /**
     * Retrieves a registered service wrapped in an {@link Optional}.
     *
     * @param clazz The class of the service to retrieve.
     * @param <T>   The type of the service.
     * @return An {@link Optional} containing the service, or empty if not registered.
     */
    default <T> @NotNull Optional<T> findService(@NotNull Class<T> clazz) {
        return Optional.ofNullable(getService(clazz));
    }

    /**
     * Retrieves a registered service or throws if absent.
     *
     * @param clazz The class of the service to retrieve.
     * @param <T>   The type of the service.
     * @return The service instance.
     * @throws IllegalStateException if the service is not registered.
     */
    default <T> @NotNull T requireService(@NotNull Class<T> clazz) {
        T service = getService(clazz);
        if (service == null) {
            throw new IllegalStateException("Required service not registered: " + clazz.getName());
        }
        return service;
    }

    /**
     * Checks if a service is currently registered.
     *
     * @param clazz The class of the service to check.
     * @return {@code true} if the service is registered.
     */
    boolean hasService(@NotNull Class<?> clazz);

    /**
     * Registers a service into the context. If a service of the same type
     * already exists, it will be skipped and a warning will be logged.
     *
     * @param service The service instance to register.
     */
    void registerService(@NotNull Object service);

    /**
     * Overrides an existing service in the context. Useful for testing or hot-swapping.
     *
     * @param service The service instance to register.
     */
    void overrideService(@NotNull Object service);

    /**
     * Returns the underlying JavaPlugin instance.
     *
     * @return The plugin instance.
     */
    @NotNull JavaPlugin getPlugin();
}