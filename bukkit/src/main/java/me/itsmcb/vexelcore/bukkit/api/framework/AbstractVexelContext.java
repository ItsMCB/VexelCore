package me.itsmcb.vexelcore.bukkit.api.framework;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractVexelContext implements VexelContext {

    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    protected AbstractVexelContext(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getService(@NotNull Class<T> clazz) {
        return (T) services.get(clazz);
    }

    @Override
    public boolean hasService(@NotNull Class<?> clazz) {
        return services.containsKey(clazz);
    }

    @Override
    public void registerService(@NotNull Object service) {
        registerInternal(service, false);
    }

    @Override
    public void overrideService(@NotNull Object service) {
        registerInternal(service, true);
    }

    private void registerInternal(@NotNull Object service, boolean override) {
        Class<?> clazz = service.getClass();

        if (override) {
            services.put(clazz, service);
        } else {
            if (services.putIfAbsent(clazz, service) != null) {
                plugin.getLogger().warning(
                        "Service " + clazz.getSimpleName() + " already registered. Skipping. Use overrideService() if intentional."
                );
                return;
            }
        }

        indexSupertypes(clazz, service, override);
    }

    private void indexSupertypes(@Nullable Class<?> clazz, @NotNull Object service, boolean override) {
        if (clazz == null || clazz == Object.class) return;

        // Register the superclass/interface itself if it's not a JDK internal type
        if (!clazz.getPackageName().startsWith("java.")) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                if (override) {
                    services.put(clazz, service);
                } else {
                    services.putIfAbsent(clazz, service);
                }
            }
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            indexSupertypes(iface, service, override);
        }

        indexSupertypes(clazz.getSuperclass(), service, override);
    }
}