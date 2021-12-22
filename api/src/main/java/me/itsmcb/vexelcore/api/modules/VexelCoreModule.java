package me.itsmcb.vexelcore.api.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VexelCoreModule {
/*
Modules will have:
- Commands
- Events
- Config and language bits
- Ability to be enabled or disabled on the fly
 */

    // General
    private String name = "Untitled Module";
    private String developer = "Unknown Developer";
    private VexelCorePlatform platform = VexelCorePlatform.UNKNOWN;
    private double version = 1.0;
    private String id = null;

    // Bukkit
    private List<Object> bukkitListenerList = new ArrayList<>();
    private Map<String, Object> bukkitCommandList = new HashMap<>();

    // Velocity
    private List<Object> velocityListenerList = new ArrayList<>();
    private Map<String, Object> velocitySimpleCommandList = new HashMap<>();


    // Setters and Getters
    // Module Info
    public void setName(String name) {
        this.name = name;
        this.id = developer + ":" + name;
    }

    public String getName() {
        return this.name;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
        this.id = developer + ":" + name;
    }

    public String getDeveloper() {
        return this.developer;
    }

    public void setPlatform(VexelCorePlatform platform) {
        this.platform = platform;
    }

    public VexelCorePlatform getPlatform() {
        return this.platform;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public double getVersion() {
        return this.version;
    }

    public String getId() {
        return this.id;
    }

    public String toString() {
        return name + " by " + developer + " for the " + platform + " platform";
    }

    // Bukkit
    public void registerBukkitListener(Object listener) {
        bukkitListenerList.add(listener);
    }

    public List<Object> getBukkitListenerList() {
        return this.bukkitListenerList;
    }

    public void registerBukkitCommand(String prefix, Object command) {
        bukkitCommandList.put(prefix, command);
    }

    public Map<String, Object> getBukkitCommandList() {
        return this.bukkitCommandList;
    }

    // Velocity
    public void registerVelocityListener(Object listener) { velocityListenerList.add(listener); }

    public List<Object> getVelocityListenerList() { return this.velocityListenerList; }

    public void registerVelocitySimpleCommand(String prefix, Object Command) {
        velocitySimpleCommandList.put(prefix, Command);
    }

    public Map<String, Object> getVelocitySimpleCommandList() {
        return velocitySimpleCommandList;
    }

}
