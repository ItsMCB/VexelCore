package me.itsmcb.vexelcore.bukkit.api.framework;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskProgress {

    public enum Strategy {
        CHAT, ACTIONBAR, TITLE
    }

    private final JavaPlugin plugin;
    private final Player player;
    private final Strategy strategy;
    private final Component prefix;
    private final long intervalTicks;

    private final AtomicInteger taskId = new AtomicInteger(-1);
    private volatile Component currentMessage = Component.empty();

    public TaskProgress(@NotNull JavaPlugin plugin, @NotNull Player player, @NotNull Strategy strategy, @NotNull String prefix, long intervalTicks) {
        this.plugin = plugin;
        this.player = player;
        this.strategy = strategy;
        this.prefix = TextUtils.mm(prefix);
        this.intervalTicks = intervalTicks;
    }

    public void start(@NotNull Component initialMessage) {
        runSync(() -> {
            this.currentMessage = initialMessage;
            sendFeedbackSync();
            this.taskId.set(Bukkit.getScheduler().runTaskTimer(plugin, this::sendFeedbackSync, intervalTicks, intervalTicks).getTaskId());
        });
    }

    public void update(@NotNull Component message) {
        this.currentMessage = message;
        sendFeedbackSync();
    }

    public void complete(@NotNull Component finalMessage) {
        runSync(() -> {
            cancelTask();
            this.currentMessage = finalMessage;
            sendFeedbackSync();
        });
    }

    public void cancel() {
        runSync(this::cancelTask);
    }

    private void cancelTask() {
        int id = taskId.getAndSet(-1);
        if (id != -1) {
            Bukkit.getScheduler().cancelTask(id);
        }
    }

    private void runSync(@NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    private void sendFeedbackSync() {
        if (!player.isOnline()) {
            cancelTask();
            return;
        }

        Component comp = prefix.append(currentMessage);
        switch (strategy) {
            case CHAT -> player.sendMessage(comp);
            case ACTIONBAR -> player.sendActionBar(comp);
            case TITLE -> player.showTitle(Title.title(
                    Component.empty(),
                    comp,
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1))
            ));
        }
    }
}