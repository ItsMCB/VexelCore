package me.itsmcb.vexelcore.velocity.modules.doorman;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class JoinEventListener {

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerChat(PostLoginEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Hello there, " + player.getUsername()));
    }

}
