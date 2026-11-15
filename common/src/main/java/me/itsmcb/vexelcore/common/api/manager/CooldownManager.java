package me.itsmcb.vexelcore.common.api.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final long cooldownMillis;
    private final Map<UUID, Long> cooldownExpirations = new ConcurrentHashMap<>();

    public CooldownManager(long cooldownSeconds) {
        this.cooldownMillis = cooldownSeconds * 1000L;
    }

    public void activateCooldown(UUID playerUUID) {
        cooldownExpirations.put(playerUUID, System.currentTimeMillis() + cooldownMillis);
    }

    public boolean isInCooldown(UUID playerUUID) {
        Long expiresAt = cooldownExpirations.get(playerUUID);
        if (expiresAt == null) {
            return false;
        }
        if (System.currentTimeMillis() >= expiresAt) {
            cooldownExpirations.remove(playerUUID, expiresAt);
            return false;
        }
        return true;
    }

}