package me.itsmcb.vexelcore.bukkit.api.managers;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import me.itsmcb.vexelcore.bukkit.api.utils.ChatUtils;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * This class is a feature-complete localization manager leveraging Adventure's component translation system, featuring variable support.
 * It copies enabled locale .yml files from the implemented plugin's /resources/lang folder to its data folder for server owner customization.
 */
public class LocalizationManager {
    private static final String LANG_DIRECTORY = "lang";
    private static final String FILE_VERSION_KEY = "file-version";

    private final JavaPlugin plugin;
    private final List<Locale> enabledLocales;
    private final String registryNamespace;
    private TranslationStore translationStore;
    private Map<Locale, Map<String, String>> dynamicTranslations = new HashMap<>();
    private Map<Locale, BoostedConfig> configCache;

    /**
     * Creates a new LocalizationManager instance.
     *
     * @param plugin The JavaPlugin instance
     * @param translationRegistryNamespace The namespace for the translation registry
     * @param enabledLocales List of enabled locales (must not be empty)
     * @throws IllegalArgumentException if enabledLocales is empty
     */
    public LocalizationManager(@NotNull JavaPlugin plugin, @NotNull String translationRegistryNamespace, @NotNull List<Locale> enabledLocales) {
        if (enabledLocales.isEmpty()) {
            throw new IllegalArgumentException("LocalizationManager must have at least one language enabled");
        }
        this.plugin = plugin;
        this.enabledLocales = List.copyOf(enabledLocales); // Immutable copy
        this.configCache = new ConcurrentHashMap<>();
        this.registryNamespace = translationRegistryNamespace;
        initializeLocalization();
    }

    private void createRegistry() {
        // File-based translations
        this.translationStore = TranslationStore.messageFormat(Key.key(registryNamespace, "translations"));
        this.translationStore.defaultLocale(enabledLocales.get(0));
    }

    /**
     * Initializes the localization system by loading configurations and registering translations.
     */
    private void initializeLocalization() {
        createRegistry();
        loadConfigurations();
        registerConfigTranslations();
        GlobalTranslator.translator().addSource(translationStore);
    }

    /**
     * Loads configuration files for all enabled locales.
     */
    private void loadConfigurations() {
        enabledLocales.forEach(locale -> {
            String resourcePath = LANG_DIRECTORY + "/" + locale + ".yml";
            try (InputStream inputStream = plugin.getResource(resourcePath)) {
                if (inputStream == null) {
                    plugin.getLogger().severe("Missing locale file: " + resourcePath);
                    return;
                }
                BoostedConfig config = new BoostedConfig(
                        plugin.getDataFolder(),
                        LANG_DIRECTORY + File.separator + locale,
                        inputStream,
                        SpigotSerializer.getInstance()
                );
                configCache.put(locale, config);
                plugin.getLogger().info("Loaded localization for: " + locale);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load locale: " + locale, e);
            }
        });
    }

    /**
     * Registers translations from loaded configurations to the registry.
     */
    private void registerConfigTranslations() {
        configCache.forEach((locale, config) -> {
            config.get().getStringRouteMappedValues(true).entrySet().stream()
                    .filter(this::isValidTranslationEntry)
                    .forEach(entry -> registerTranslationEntry(locale, entry.getKey(), entry.getValue()));
        });
    }

    /**
     * Registers a translation that isn't file-based.
     */
    public void registerDynamicTranslation(@NotNull Locale locale, @NotNull String key, @NotNull String message) {
        try {
            Map<String, String> localeStrings = dynamicTranslations.computeIfAbsent(locale, l -> new ConcurrentHashMap<>());
            localeStrings.put(key, message);
            translationStore.register(
                    key,
                    locale,
                    new MessageFormat(message)
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register translation for key '" + key + "', locale '" + locale + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidTranslationEntry(Map.Entry<String, Object> entry) {
        return !FILE_VERSION_KEY.equals(entry.getKey()) &&
                !(entry.getValue() instanceof Section) &&
                !String.valueOf(entry.getValue()).contains("libs.dev.dejvokep.boostedyaml.block.implementation.Section");
    }

    private void registerTranslationEntry(Locale locale, String key, Object value) {
        try {
            translationStore.register(
                    key,
                    locale,
                    new MessageFormat(String.valueOf(value))
            );
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to register translation: " + key, e);
        }
    }

    /**
     * Removes translations from translation store.
     */
    public void unregisterTranslationEntry(String key) {
        translationStore.unregister(key);
    }

    /**
     * Reloads all localizations.
     */
    public void reload() {
        GlobalTranslator.translator().removeSource(translationStore);
        configCache.clear();
        initializeLocalization();
    }

    /**
     * Sends a localized and colorized message to a player.
     *
     * @param player The player to send the message to
     * @param path The translation path
     */
    public void sendMsg(@NotNull Player player, @NotNull String path) {
        player.sendMessage(getComponent(player, path));
    }

    /**
     * Sends a localized and colorized message with arguments to a player.
     *
     * @param player The player to send the message to
     * @param path The translation path
     * @param arguments The arguments to insert into the translation
     */
    public void sendMsg(@NotNull Player player, @NotNull String path, @NotNull ComponentLike... arguments) {
        player.sendMessage(getComponent(player, path, arguments));
    }

    /**
     * Gets a localized and colorized component for a player.
     *
     * @param player The player to get the localization for
     * @param path The translation path
     * @return The localized component
     */
    public Component getComponent(@NotNull Player player, @NotNull String path) {
        return ChatUtils.colorizeComponentText(GlobalTranslator.render(Component.translatable(path), player.locale()));
    }

    /**
     * Gets a localized and colorized component with arguments for a player.
     *
     * @param player The player to get the localization for
     * @param path The translation path
     * @param arguments The arguments to insert into the translation
     * @return The localized component with arguments
     */
    public Component getComponent(@NotNull Player player, @NotNull String path, @NotNull ComponentLike... arguments) {
        return ChatUtils.colorizeComponentText(GlobalTranslator.render(
                Component.translatable(path).arguments(arguments),
                player.locale()
        ));
    }

    /**
     * Gets a localized and colorized string for a player.
     *
     * @param player The player to get the localization for
     * @param path The translation path
     * @return The localized string
     */
    public String getString(@NotNull Player player, @NotNull String path) {
        return ChatUtils.getColorizer().serialize(getComponent(player, path));
    }

    /**
     * Gets a localized and colorized string for a player.
     *
     * @param player The player to get the localization for
     * @param path The translation path
     * @param arguments The arguments
     * @return The localized string
     */
    public String getString(@NotNull Player player, @NotNull String path, @NotNull ComponentLike... arguments) {
        return ChatUtils.getColorizer().serialize(getComponent(player,path,arguments));
    }

    /**
     * Checks if a key has been registered.
     *
     * @param key The key to check
     * @return If the key has any registered translation.
     */
    public boolean hasString(@NotNull String key) {
        return translationStore.contains(key);
    }
}