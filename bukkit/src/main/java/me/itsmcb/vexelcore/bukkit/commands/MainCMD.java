package me.itsmcb.vexelcore.bukkit.commands;

import me.itsmcb.vexelcore.api.modules.VexelCoreModule;
import me.itsmcb.vexelcore.api.utils.CMDHelper;
import me.itsmcb.vexelcore.api.utils.Icon;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import me.itsmcb.vexelcore.bukkit.api.utils.BukkitUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainCMD implements CommandExecutor, TabCompleter {

    VexelCoreBukkit instance;

    public MainCMD(VexelCoreBukkit instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CMDHelper cmdHelper = new CMDHelper(args);
        if (cmdHelper.isCalling("about")) {
            BukkitUtils.send(sender, "&8========== &c" + Icon.DOUBLE_ARROW_RIGHT + " &6VexelCore-Bukkit &c" + Icon.DOUBLE_ARROW_LEFT + " &8==========");
            BukkitUtils.send(sender, "&7Version: &6@version@");
            BukkitUtils.send(sender, "&7Developer: &6ItsMCB");
            BukkitUtils.send(sender, BukkitUtils.interactiveMessageOpenWebsite(
                    "&7Source: &6https://github.com/ItsMCB/VexelCore",
                    "Click to open website",
                    "https://github.com/ItsMCB/VexelCore"));
        }
        if (cmdHelper.isCalling("reload") && sender.hasPermission("vexelcore.admin")) {
            instance.getModuleHandler().disableAllModules();
            instance.getModuleHandler().loadLocalModules();
            BukkitUtils.send(sender, "&aReload complete");
        }
        if (cmdHelper.isCalling("module") && sender.hasPermission("vexelcore.admin")) {
            if (cmdHelper.argEquals(1, "list")) {
                BukkitUtils.send(sender, "&8========== &c" + Icon.DOUBLE_ARROW_RIGHT + " &6VexelCore-Bukkit Modules &c" + Icon.DOUBLE_ARROW_LEFT + " &8==========");
                for(int i = 0; i < instance.getModuleHandler().getModuleList().size(); i++) {
                    VexelCoreModule module = instance.getModuleHandler().getModuleList().get(i);
                    BukkitUtils.send(sender, "&c" + (i+1) + "&7) &6" + module.getName() + " " + module.getVersion() +" - " + module.getDeveloper());
                }
                return true;
            }
            if (cmdHelper.argEquals(1, "about")) {
                if (cmdHelper.argExists(2)) {
                    String[] id = args[2].split(":");
                    if (!(id.length > 1)) {
                        return true;
                    }
                    Optional<VexelCoreModule> optionalModule = instance.getModuleHandler().getModule(id[0],id[1]);
                    if (optionalModule.isPresent()) {
                        VexelCoreModule module = optionalModule.get();
                        BukkitUtils.send(sender, "&8========== &c" + Icon.DOUBLE_ARROW_RIGHT + " &6VexelCore-Bukkit Module Info &c" + Icon.DOUBLE_ARROW_LEFT + " &8==========");
                        BukkitUtils.send(sender, "&7Name: &6" + module.getName());
                        BukkitUtils.send(sender, "&7Developer: &6" + module.getDeveloper());
                        BukkitUtils.send(sender, "&7Version: &6" + module.getVersion());
                        BukkitUtils.send(sender, "&7ID: &6" + module.getId());
                        BukkitUtils.send(sender, "&7Platform: &6" + module.getPlatform());
                        if (module.getBukkitCommandList().size() != 0) {
                            BukkitUtils.send(sender, "&7Registered Commands:");
                            sendCommandInfo(sender, module.getBukkitCommandList());
                        }
                        if (module.getBukkitListenerList().size() != 0) {
                            BukkitUtils.send(sender, "&7Registered Listeners:");
                            sendListenerInfo(sender, module.getBukkitListenerList());
                        }
                        return true;
                    }
                    BukkitUtils.send(sender, "&cCouldn't find that module");
                }
                return true;
            }
            // TODO Send usage
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CMDHelper cmdHelper = new CMDHelper(args);
        cmdHelper.addTabCompletion(cmdHelper.getMap(0,null),List.of("about","module","reload"));
        cmdHelper.addTabCompletion(cmdHelper.getMap(1,"module"),List.of("list","about"));
        cmdHelper.addTabCompletion(cmdHelper.getMap(2, "about"), instance.getModuleHandler().getModuleList().stream().map(VexelCoreModule::getId).toList());
        return cmdHelper.generateTabComplete();
    }

    private void sendCommandInfo(CommandSender sender, Map<String, Object> bukkitCommandList) {
        for (Command cmd : bukkitCommandList.values().stream().map(o -> ((Command) o)).toList()) {
            BukkitUtils.send(sender, "&8- &7Name: " + cmd.getName());
            BukkitUtils.send(sender, "  &8- &7Description: " + cmd.getDescription());
            BukkitUtils.send(sender, "  &8- &7Aliases: " + cmd.getAliases());
            BukkitUtils.send(sender, "  &8- &7Timings Name: " + cmd.getTimingName());
            BukkitUtils.send(sender, "  &8- &7Class Name: " + cmd.getClass().getName());
        }
    }

    private void sendListenerInfo(CommandSender sender, List<Object> bukkitListenerList) {
        for (Listener listener : bukkitListenerList.stream().map(o -> ((Listener) o)).toList()) {
            BukkitUtils.send(sender, "&8- &7Class Name: " + listener.getClass().getName());
        }
    }

}
