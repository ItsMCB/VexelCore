package me.itsmcb.vexelcore.common.api.manager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class CooldownManager {

    private final long cooldownTime;
    private ArrayList<UUID> playerUUIDsInCooldown = new ArrayList<>();

    public CooldownManager (long cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public void activateCooldown(UUID playerUUID) {
        playerUUIDsInCooldown.add(playerUUID);
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        playerUUIDsInCooldown.remove(playerUUID);
                    }
                },
                1000*cooldownTime
        );
    }

    public boolean isInCooldown(UUID playerUUID) {
        return playerUUIDsInCooldown.contains(playerUUID);
    }

}

