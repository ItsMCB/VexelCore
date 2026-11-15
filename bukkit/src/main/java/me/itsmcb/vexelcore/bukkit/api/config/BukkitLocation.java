package me.itsmcb.vexelcore.bukkit.api.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@SerializableAs("BukkitLocation")
public class BukkitLocation extends BukkitCoordinates {

    private String worldName;

    public BukkitLocation() {}

    public BukkitLocation(Location location) {
        super(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        if (location.getWorld() != null) {
            this.worldName = location.getWorld().getName();
        }
    }

    public BukkitLocation(String name, double x, double y, double z, float yaw, float pitch) {
        super(x,y,z,yaw,pitch);
        this.worldName = name;
    }

    /**
     * Removes legacy Bukkit Location serialization tags from a YAML file before loading
     * so unloaded dimensions (e.g. Voyage worlds) do not fail {@code Location.deserialize} on boot.
     */
    public static void stripLegacyLocationTags(@NotNull File configFile) {
        if (!configFile.isFile()) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
            boolean modified = false;
            List<String> cleaned = new ArrayList<>(lines.size());
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.equals("==: org.bukkit.Location")
                        || trimmed.equals("==: \"org.bukkit.Location\"")
                        || trimmed.equals("==: 'org.bukkit.Location'")) {
                    modified = true;
                    continue;
                }
                cleaned.add(line);
            }
            if (modified) {
                Files.write(configFile.toPath(), cleaned, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to migrate legacy Location tags in " + configFile.getName(), e);
        }
    }

    /**
     * Constructs location. Important: Assumes world is loaded!
     * @return Location
     */
    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldName),getX(),getY(),getZ(),getYaw(),getPitch());
    }

    public String getWorldName() {
        return worldName;
    }

    public BukkitLocation setWorldName(String worldName) {
        this.worldName = worldName;
        return this;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        if (worldName != null) {
            map.put("name", worldName);
        }
        map.put("x", getX() + "");
        map.put("y", getY() + "");
        map.put("z", getZ() + "");
        map.put("yaw", getYaw() + "");
        map.put("pitch", getPitch() + "");
        return map;
    }

    public static BukkitLocation deserialize(@NotNull Map<String, Object> map) {
        try {
            BukkitLocation bl = new BukkitLocation();
            if (map.get("name") instanceof String name) {
                bl.setWorldName(name);
            } else if (map.get("world") instanceof String world) {
                bl.setWorldName(world);
            }
            if (map.containsKey("x")) {
                bl.setX(parseDouble(map.get("x"), 0.0));
            }
            if (map.containsKey("y")) {
                bl.setY(parseDouble(map.get("y"), 64.0));
            }
            if (map.containsKey("z")) {
                bl.setZ(parseDouble(map.get("z"), 0.0));
            }
            if (map.containsKey("yaw")) {
                bl.setYaw((float) parseDouble(map.get("yaw"), 0.0));
            }
            if (map.containsKey("pitch")) {
                bl.setPitch((float) parseDouble(map.get("pitch"), 0.0));
            }
            return bl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}