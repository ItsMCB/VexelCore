package me.itsmcb.vexelcore.bukkit.api.managers;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import me.itsmcb.vexelcore.bukkit.api.utils.ChatUtils;
import me.itsmcb.vexelcore.common.api.config.BoostedConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

public class LocalizationManager {

    public LocalizationManager(@NotNull JavaPlugin plugin, @NotNull String translationRegisteryKeyNamespace, @NotNull List<Locale> enabledLocales) {
        if (enabledLocales.isEmpty()) {
            plugin.getLogger().severe("LocalizationManager must have at least one language enabled to function!");
            return;
        }

        TranslationRegistry registry = TranslationRegistry.create(Key.key(translationRegisteryKeyNamespace, "translations"));
        registry.defaultLocale(enabledLocales.get(0)); // The first supported locale will be the fallback

        // Transfer data from config files to global translation system
        enabledLocales.forEach(locale -> {
            InputStream inputStreamFile = plugin.getResource("lang/"+locale+".yml");
            if (inputStreamFile == null) {
                plugin.getLogger().severe("The "+locale+" locale is enabled but cannot be found in the jar resources.");
                return;
            }
            BoostedConfig file = new BoostedConfig(plugin.getDataFolder(),"lang"+ File.separator+locale, inputStreamFile, SpigotSerializer.getInstance());
            file.get().getStringRouteMappedValues(true).entrySet().stream()
                    .filter(entry -> {
                        // Skip file-version
                        if (entry.getKey().equals("file-version")) {
                            return false;
                        }
                        // Skip Section objects
                        if (entry.getValue() instanceof Section || String.valueOf(entry.getValue()).contains("libs.dev.dejvokep.boostedyaml.block.implementation.Section")) {
                            return false;
                        }
                        return true;
                    })
                    .forEach(entry -> {
                        plugin.getLogger().warning("Logging "+entry.getKey()+ " | "+entry.getValue());
                        registry.register(
                                entry.getKey(),
                                locale,
                                new MessageFormat(String.valueOf(entry.getValue()))
                        );
                    });
            GlobalTranslator.translator().addSource(registry);
            plugin.getLogger().info("Successfully saved and loaded localization \""+locale+"\"");
        });
    }

    public Component getComponent(Player player, String path) {
        // Serialize to turn component into a string, then deserialize to turn it back into a component, now including & and hex
        return ChatUtils.getColorizer().deserialize(ChatUtils.getColorizer().serialize(GlobalTranslator.render(Component.translatable(path),player.locale())));
    }

    public Component getComponent(Player player, String path, ComponentLike... arguments) {
        return ChatUtils.getColorizer().deserialize(ChatUtils.getColorizer().serialize(GlobalTranslator.render(Component.translatable(path).arguments(arguments),player.locale())));
    }

    public String getString(Player player, String path) {
        return ChatUtils.getColorizer().serialize(getComponent(player,path));
    }
}