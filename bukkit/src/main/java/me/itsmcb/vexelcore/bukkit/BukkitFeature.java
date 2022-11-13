package me.itsmcb.vexelcore.bukkit;

import me.itsmcb.vexelcore.common.api.element.VexelCoreFeature;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class BukkitFeature extends VexelCoreFeature {

    private ArrayList<Listener> listeners = new ArrayList<>();
    private ArrayList<Command> commands = new ArrayList<>();
    private String configPath;
    private JavaPlugin instance;
    private int delay = 0;

    public BukkitFeature(String name, String description, String configPath, JavaPlugin instance) {
        super(name, description);
        this.configPath = configPath;
        this.instance = instance;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public FileConfiguration getConfig() {
        return instance.getConfig();
    }


    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void registerCommand(Command command) {
        commands.add(command);
    }

    @Override
    public void enable() {
        if (delay > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    enableWithConfigCheck();
                }
            }.runTaskLater(instance, delay);
        } else {
            enableWithConfigCheck();
        }
    }

    @Override
    public void disable() {
        if (getStatus().equals(FeatureStatus.ENABLED)) {
            setStatus(FeatureStatus.DISABLED);
            listeners.forEach(listener -> {
                HandlerList.unregisterAll(listener);
                log(ElementType.BUKKITLISTENER, ElementState.UNREGISTERING, getName());
            });
            commands.forEach(command -> {
                Bukkit.getCommandMap().getKnownCommands().remove(command.getName());
                Bukkit.getCommandMap().getKnownCommands().remove(command.getName()+":"+command.getName());
            });
            disableTriggers();
        }
    }

    public void enableWithConfigCheck() {
        if (configPath == null) {
            enableLogic();
        } else {
            if (instance.getConfig().getBoolean(configPath+".enabled")) {
                enableLogic();
            }
        }
    }

    private void enableLogic() {
        setStatus(FeatureStatus.ENABLED);
        listeners.forEach(listener -> {
            Bukkit.getPluginManager().registerEvents(listener, instance);
            log(ElementType.BUKKITLISTENER, ElementState.REGISTERING, getName());
        });
        commands.forEach(command -> {
            try {
                Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);
                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
                commandMap.register(command.getName(), command);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.out.println("ERROR ENABLING FEATURE CALLED " + getName());
                e.printStackTrace();
            }
        });
        enableTriggers();
    }
}
