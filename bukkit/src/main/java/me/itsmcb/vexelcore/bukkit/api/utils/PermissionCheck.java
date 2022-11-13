package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.command.CommandSender;

public class PermissionCheck {

    String permission;
    CommandSender sender;

    public PermissionCheck(String permission, CommandSender sender) {
        this.permission = permission;
        this.sender = sender;
    }

    public boolean hasPermission() {
        return sender.hasPermission(permission);
    }

}
