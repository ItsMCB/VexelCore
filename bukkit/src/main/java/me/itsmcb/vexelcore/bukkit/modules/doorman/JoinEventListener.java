package me.itsmcb.vexelcore.bukkit.modules.doorman;

import me.itsmcb.vexelcore.bukkit.utils.BukkitUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEventListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(BukkitUtils.componentize("&7Welcome, &3" + event.getPlayer().getName()));
    }

}
