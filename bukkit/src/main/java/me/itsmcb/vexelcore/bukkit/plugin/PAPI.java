package me.itsmcb.vexelcore.bukkit.plugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PAPI extends PlaceholderExpansion {

    private VexelCoreBukkit instance;
    public PAPI(VexelCoreBukkit instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "vexelcore";
    }

    @Override
    public String getAuthor(){
        return instance.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("version")) {
            return instance.getDescription().getVersion();
        }
        if (params.equalsIgnoreCase("servers")) {
            return String.join(", ", instance.getProxyManager().getServerNames());
        }
        if (params.equalsIgnoreCase("player_uuid")) {
            return player.getUniqueId().toString();
        }
        if (params.contains("player_brand_by_uuid_")) {
            String input = params.replace("player_brand_by_uuid_", "");
            return instance.getProxyManager().getPlayerBrand(UUID.fromString(input));
        }
        return null;
    }
}
