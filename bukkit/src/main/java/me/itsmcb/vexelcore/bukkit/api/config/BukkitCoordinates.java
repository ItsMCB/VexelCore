package me.itsmcb.vexelcore.bukkit.api.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("BukkitCoordinates")
public class BukkitCoordinates implements ConfigurationSerializable {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public BukkitCoordinates() {}

    public BukkitCoordinates(Location location) {
        this(location.getX(),location.getY(), location.getZ());
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public BukkitCoordinates(double x, double y, double z, float yaw, float pitch) {
        this(x,y,z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public BukkitCoordinates(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location getLocation(World world) {
        return new Location(world,x,y,z,yaw,pitch);
    }

    public Location getLocation(String loadedWorldName) {
        return new Location(Bukkit.getWorld(loadedWorldName),x,y,z,yaw,pitch);
    }

    public double getX() {
        return x;
    }

    public BukkitCoordinates setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public BukkitCoordinates setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return z;
    }

    public BukkitCoordinates setZ(double z) {
        this.z = z;
        return this;
    }

    public float getYaw() {
        return yaw;
    }

    public BukkitCoordinates setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public float getPitch() {
        return pitch;
    }

    public BukkitCoordinates setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("x",x+"");
        map.put("y",y+"");
        map.put("z",z+"");
        map.put("yaw",yaw+"");
        map.put("pitch",pitch+"");
        return map;
    }

    public static BukkitCoordinates deserialize(@NotNull Map<String, Object> map) {
        try {
            BukkitCoordinates bl = new BukkitCoordinates();
            if (map.containsKey("x")) {
                bl.setX(Double.parseDouble((String) map.get("x")));
            }
            if (map.containsKey("y")) {
                bl.setY(Double.parseDouble((String) map.get("y")));
            }
            if (map.containsKey("z")) {
                bl.setZ(Double.parseDouble((String) map.get("z")));
            }
            if (map.containsKey("yaw")) {
                bl.setYaw(Float.parseFloat((String) map.get("yaw")));
            }
            if (map.containsKey("pitch")) {
                bl.setPitch(Float.parseFloat((String) map.get("pitch")));
            }
            return bl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
