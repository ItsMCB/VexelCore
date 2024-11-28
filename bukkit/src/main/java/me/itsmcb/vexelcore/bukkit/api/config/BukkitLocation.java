package me.itsmcb.vexelcore.bukkit.api.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("BukkitLocation")
public class BukkitLocation implements ConfigurationSerializable {

    private String name;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public BukkitLocation() {}

    public BukkitLocation(Location location) {
        this.name = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public BukkitLocation(String name, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(name),x,y,z,yaw,pitch);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("name",name);
        map.put("x",x+"");
        map.put("y",y+"");
        map.put("z",z+"");
        map.put("yaw",yaw+"");
        map.put("pitch",pitch+"");
        return map;
    }

    public static BukkitLocation deserialize(@NotNull Map<String, Object> map) {
        try {
            BukkitLocation bl = new BukkitLocation();
            if (map.containsKey("name")) {
                bl.setName((String) map.get("name"));
            }
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
