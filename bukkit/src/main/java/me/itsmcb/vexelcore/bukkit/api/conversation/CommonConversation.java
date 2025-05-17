package me.itsmcb.vexelcore.bukkit.api.conversation;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for creating and configuring Bukkit conversations with common defaults.
 */
public class CommonConversation {

    public static class Builder {
        private final JavaPlugin plugin;
        private final Prompt firstPrompt;
        private final Player player;
        private final Map<Object, Object> sessionData = new HashMap<>();
        private String escapeSequence = "exit";
        private int timeout = 60;

        public Builder(@NotNull JavaPlugin plugin, @NotNull Prompt firstPrompt, @NotNull Player player) {
            this.plugin = plugin;
            this.firstPrompt = firstPrompt;
            this.player = player;
        }

        /**
         * Input text to escape conversation (ex. "exit").
         *
         * @param escapeSequence The escape sequence
         * @return This builder for chaining
         */
        public Builder withEscapeSequence(String escapeSequence) {
            this.escapeSequence = escapeSequence;
            return this;
        }

        /**
         * Sets conversation timeout.
         *
         * @param timeout Timeout in seconds
         * @return This builder for chaining
         */
        public Builder withTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Adds a single entry to the session data.
         *
         * @param key The key for the session data
         * @param value The value for the session data
         * @return This builder for chaining
         */
        public Builder withSessionData(Object key, Object value) {
            this.sessionData.put(key, value);
            return this;
        }

        /**
         * Adds multiple entries to the session data.
         *
         * @param data Map containing the session data to add
         * @return This builder for chaining
         */
        public Builder withSessionData(Map<Object, Object> data) {
            this.sessionData.putAll(data);
            return this;
        }

        /**
         * Builds and begins the conversation.
         *
         * @return The created and started conversation
         */
        public Conversation begin() {
            ConversationFactory factory = new ConversationFactory(plugin)
                    .withFirstPrompt(firstPrompt)
                    .withEscapeSequence(escapeSequence)
                    .withTimeout(timeout)
                    .withPrefix(new InputPrefix())
                    .withLocalEcho(false)
                    .addConversationAbandonedListener(new ConversationEnd());
            Conversation conversation = factory.buildConversation(player);
            sessionData.forEach((k,v) -> {
                conversation.getContext().setSessionData(k,v);
            });
            conversation.begin();
            return conversation;
        }
    }

    /**
     * Creates a new conversation builder.
     *
     * @param instance The plugin instance
     * @param firstPrompt The first prompt to show in the conversation
     * @param player The player to have the conversation with
     * @return A new builder to configure and start the conversation
     */
    public static Builder create(JavaPlugin instance, Prompt firstPrompt, Player player) {
        return new Builder(instance, firstPrompt, player);
    }
}