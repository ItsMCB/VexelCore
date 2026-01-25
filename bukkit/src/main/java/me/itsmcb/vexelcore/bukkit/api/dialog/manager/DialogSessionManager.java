package me.itsmcb.vexelcore.bukkit.api.dialog.manager;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DialogSessionManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Set<Key>> activePlayerKeys = new ConcurrentHashMap<>();
    private final Map<Key, SessionCallback> callbacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> debounceMap = new ConcurrentHashMap<>();
    private final boolean hasFloodgate;

    public DialogSessionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.hasFloodgate = Bukkit.getPluginManager().isPluginEnabled("Floodgate");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::runGarbageCollector, 6000L, 6000L);
    }

    /**
     * Checks if the player's client (?) is spamming menu opens.
     * Dynamic limits based on client type.
     * Primary used to fix a Bedrock (/Geyser?) issue/bug.
     */
    public boolean isDebounced(Player player) {
        long lastOpen = debounceMap.getOrDefault(player.getUniqueId(), 0L);
        long now = System.currentTimeMillis();

        // Determine Cooldown
        long cooldown = 10L; // Default Java: 10ms (likely unnecessary - keeping temporarily for good measure)

        if (hasFloodgate && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            cooldown = 500L; // Bedrock: 500ms (Prevents 7 attempts Geyser error)
        }

        if (now - lastOpen < cooldown) {
            return true;
        }

        debounceMap.put(player.getUniqueId(), now);
        return false;
    }

    /**
     * Opens a dialog for a player with a debounce check
     */
    public void openDialog(Player player, Runnable dialogOpener) {
        if (isDebounced(player)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(VexelCoreBukkit.getInstance(), () -> {

            // Close container if open (i.e., chest menu)
            // Primary to prevent Bedrock client issues
            if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
                player.closeInventory();
            }

            // Determine delay based on client type
            boolean isBedrock = Bukkit.getPluginManager().isPluginEnabled("Floodgate")
                    && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());

            long openDelay = isBedrock ? 12L : 1L;

            Bukkit.getScheduler().runTaskLater(VexelCoreBukkit.getInstance(), () -> {
                startNewSession(player.getUniqueId());
                try {
                    dialogOpener.run();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error opening dialog for " + player.getName());
                    e.printStackTrace();
                }
            }, openDelay);

        }, 1L);
    }

    public void startNewSession(UUID playerId) {
        Set<Key> oldKeys = activePlayerKeys.remove(playerId);
        if (oldKeys != null) {
            oldKeys.forEach(callbacks::remove);
        }
    }

    public Key addCallback(UUID playerId, Consumer<PlayerCustomClickEvent> action) {
        activePlayerKeys.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());
        Key key = Key.key("vexel", "btn/" + UUID.randomUUID());
        callbacks.put(key, new SessionCallback(action, Instant.now()));
        activePlayerKeys.get(playerId).add(key);
        return key;
    }

    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();
        Key id = event.getIdentifier();

        Set<Key> validKeys = activePlayerKeys.get(player.getUniqueId());
        if (validKeys != null && validKeys.contains(id)) {
            SessionCallback session = callbacks.get(id);
            if (session != null) {
                try {
                    session.action.accept(event);
                } catch (Exception e) {
                    plugin.getLogger().severe("Error processing Dialog callback for " + player.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        startNewSession(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uid = event.getPlayer().getUniqueId();
        startNewSession(uid);
        debounceMap.remove(uid);
    }

    private void runGarbageCollector() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.MINUTES);
        callbacks.entrySet().removeIf(e -> e.getValue().createdAt.isBefore(cutoff));

        activePlayerKeys.entrySet().removeIf(entry -> entry.getValue().isEmpty() || plugin.getServer().getPlayer(entry.getKey()) == null);

        long debounceCutoff = System.currentTimeMillis() - 3600000;
        debounceMap.values().removeIf(timestamp -> timestamp < debounceCutoff);
    }

    private record SessionCallback(Consumer<PlayerCustomClickEvent> action, Instant createdAt) {}
}