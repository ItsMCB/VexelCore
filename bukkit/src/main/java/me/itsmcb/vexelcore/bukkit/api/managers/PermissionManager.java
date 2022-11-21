package me.itsmcb.vexelcore.bukkit.api.managers;


import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class PermissionManager {

    private HashMap<String, String> permissions = new HashMap<>();

    public void set(String id, String permission) {
        permissions.put(id, permission);
    }

    public void get(String id) {
        permissions.get(id);
    }

    public boolean has(CommandSender sender, String id) {
        return sender.hasPermission(permissions.get(id));
    }

    public boolean lacks(CommandSender sender, String id) {
        return !has(sender, id);
    }
}
