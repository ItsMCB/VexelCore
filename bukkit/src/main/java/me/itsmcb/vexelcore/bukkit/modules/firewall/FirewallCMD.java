package me.itsmcb.vexelcore.bukkit.modules.firewall;

import me.itsmcb.vexelcore.api.utils.MojangUtils;
import me.itsmcb.vexelcore.bukkit.api.utils.BukkitUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FirewallCMD extends Command {

    public FirewallCMD(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("checkuuid")) {
                try {
                    BukkitUtils.send(sender, "UUID of \"" + args[1] + "\" exists? " + MojangUtils.uuidExists(args[1]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            sender.sendMessage("Usage: /firewall checkuuid <UUID>");
        }
        return false;
    }
}
