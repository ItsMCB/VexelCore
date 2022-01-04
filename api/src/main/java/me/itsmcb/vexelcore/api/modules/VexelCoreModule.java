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

    public VexelCoreModule(String name, String developer, VexelCorePlatform platform, double version) {
        this.name = name;
        this.developer = developer;
        this.id = developer + ":" + name;
        this.platform = platform;
        this.version = version;
    }

    // General
    private String name;
    private String developer;
    private VexelCorePlatform platform;
    private String id;
    private double version;
    private List<String> pluginDependencies = new ArrayList<>();

    // Bukkit
    private List<Object> bukkitListenerList = new ArrayList<>();
    private Map<String, Object> bukkitCommandList = new HashMap<>();

    // Velocity
    private List<Object> velocityListenerList = new ArrayList<>();
    private Map<String, Object> velocitySimpleCommandList = new HashMap<>();


    // Setters and Getters
    // Module Info

    public String getName() {
        return this.name;
    }

    public String getDeveloper() {
        return this.developer;
    }

    public VexelCorePlatform getPlatform() {
        return this.platform;
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


    // Platform Plugin Dependencies
    public List<String> getPluginDependencies() {
        return pluginDependencies;
    }

    public void addPluginDependency(String dependencyName) {
        this.pluginDependencies.add(dependencyName);
    }

    // TODO Other Module Dependencies

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
