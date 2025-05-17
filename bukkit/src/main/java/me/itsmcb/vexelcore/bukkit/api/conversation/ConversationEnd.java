package me.itsmcb.vexelcore.bukkit.api.conversation;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConversationEnd implements ConversationAbandonedListener {

    @Override
    public void conversationAbandoned(@NotNull ConversationAbandonedEvent e) {
        Player player = (Player) e.getContext().getForWhom();
        if (e.gracefulExit()) {
            return;
        }
        new BukkitMsgBuilder("&cChat input has timed out. Please try again later if need be").send(player);
    }
}
