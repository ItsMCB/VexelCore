package me.itsmcb.vexelcore.bukkit.api.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EntityUtils {

    public static List<Entity> getNearEntities(Entity entity, int range) {
        return entity.getNearbyEntities(range, range, range);
    }

    public static List<Entity> getNearLivingEntities(Entity entity, int range, boolean includePlayers) {
        List<Entity> entities = new ArrayList<>();
        for(Entity foundEntity : getNearEntities(entity, range)){
            if (includePlayers && foundEntity instanceof Player) {
                entities.add(foundEntity);
            }
            if (foundEntity instanceof LivingEntity) {
                entities.add(foundEntity);
            }
        }
        return entities;
    }



}
