package me.itsmcb.vexelcore.bukkit.api.conversation;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;
import org.jetbrains.annotations.NotNull;

public class InputPrefix implements ConversationPrefix {
    @Override
    public @NotNull String getPrefix(@NotNull ConversationContext context) {
        return ChatColor.LIGHT_PURPLE+"Input Prompt"+ChatColor.WHITE+": "+ChatColor.GRAY;
    }
}
