package me.itsmcb.vexelcore.bukkit.api.managers;

import me.itsmcb.vexelcore.bukkit.BukkitFeature;

import java.util.ArrayList;

public class BukkitFeatureManager {

    private ArrayList<BukkitFeature> features = new ArrayList<>();

    public void register(BukkitFeature feature) {
        features.add(feature);
    }

    public void unregister(BukkitFeature feature) {
        features.remove(feature);
    }

    public void reload() {
        features.forEach(BukkitFeature::disable);
        features.forEach(BukkitFeature::enable);
    }

}
