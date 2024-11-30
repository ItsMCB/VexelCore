package me.itsmcb.vexelcore.bukkit.api.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("BukkitLocation")
public class BukkitLocation extends BukkitCoordinates {

    private String name;

    public BukkitLocation() {}

    public BukkitLocation(Location location) {
        super(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.name = location.getWorld().getName();
    }

    public BukkitLocation(String name, double x, double y, double z, float yaw, float pitch) {
        super(x,y,z,yaw,pitch);
        this.name = name;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(name),getX(),getY(),getZ(),getYaw(),getPitch());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("x",getX()+"");
        map.put("y",getY()+"");
        map.put("z",getZ()+"");
        map.put("yaw",getYaw()+"");
        map.put("pitch",getPitch()+"");
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
