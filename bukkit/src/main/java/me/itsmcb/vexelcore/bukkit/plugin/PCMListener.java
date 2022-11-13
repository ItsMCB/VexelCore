package me.itsmcb.vexelcore.bukkit.plugin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PCMListener implements PluginMessageListener {

    private VexelCoreBukkit instance;

    public PCMListener(VexelCoreBukkit instance) {
        this.instance = instance;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        // Client Brand
        if (channel.equals("minecraft:brand")) {
            String brand = new String(message, StandardCharsets.UTF_8).substring(1);
            instance.getProxyManager().setPlayerBrand(player.getUniqueId(), brand);
        }
        // Bungeecord or Velocity w/support enabled
        if (channel.equals("BungeeCord")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();
            if(subchannel.equalsIgnoreCase("GetServers")) {
                instance.getProxyManager().setServerNames(Arrays.asList(in.readUTF().split(", ")));
            }
        }
    }
}
