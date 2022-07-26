package me.itsmcb.vexelcore.bukkit.plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ProxyManager implements Listener {

    private VexelCoreBukkit instance;

    public ProxyManager(VexelCoreBukkit instance) {
        this.instance = instance;
        refreshServerNames();
    }

    private List<String> serverNames = new ArrayList<>();

    public void setServerNames(List<String> serverNames) {
        this.serverNames = serverNames;
    }

    public List<String> getServerNames() {
        return serverNames;
    }

    public void refreshServerNames() {
        ByteArrayDataOutput outGS = ByteStreams.newDataOutput();
        outGS.writeUTF("GetServers");
        Bukkit.getServer().sendPluginMessage(instance, "BungeeCord", outGS.toByteArray());
    }

    private HashMap<UUID, String> playerBrands = new HashMap<>();

    public void setPlayerBrand(UUID playerUUID, String brand) {
        this.playerBrands.put(playerUUID, brand);
    }

    public String getPlayerBrand(UUID playerUUID) {
        return playerBrands.get(playerUUID);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                refreshServerNames();
            }
        }.runTaskLater(instance, 20L);
    }
}