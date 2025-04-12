package me.itsmcb.vexelcore.bukkit.api.utils;

import me.itsmcb.vexelcore.bukkit.api.menuv2.MenuV2Item;
import me.itsmcb.vexelcore.bukkit.api.menuv2.SkullBuilder;
import me.itsmcb.vexelcore.bukkit.plugin.CachedPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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

    public static MenuV2Item getMenuRepresentation(Entity entity) {
        Material tempMat = Material.getMaterial(entity.getType().getKey().value().toUpperCase() + "");
        if (tempMat == null) {
            tempMat = Material.getMaterial(entity.getType().getKey().value().toUpperCase() + "_SPAWN_EGG");
        }
        if (entity instanceof Item item) {
            tempMat = item.getItemStack().getType();
        }
        if (tempMat == null) {
            tempMat = Material.PAPER;
        }
        MenuV2Item item = new MenuV2Item(tempMat);
        switch (entity.getType()) {
            // TODO skulls for every type (likely need to use skull creator
            case PLAYER -> item = new SkullBuilder((Player) entity);
            case CREEPER -> item.material(Material.CREEPER_HEAD);
            case SKELETON -> item.material(Material.SKELETON_SKULL);
            case ZOMBIE -> item.material(Material.ZOMBIE_HEAD);
            case SHEEP -> item = new SkullBuilder(new CachedPlayer("MHF_Sheep"));
            case BLAZE -> item = new SkullBuilder(new CachedPlayer("MHF_Blaze"));
            case CAVE_SPIDER -> item = new SkullBuilder(new CachedPlayer("MHF_CaveSpider"));
            case CHICKEN -> item = new SkullBuilder(new CachedPlayer("MHF_Chicken"));
            case COW -> item = new SkullBuilder(new CachedPlayer("MHF_Cow"));
            case ENDERMAN -> item = new SkullBuilder(new CachedPlayer("MHF_Enderman"));
            case GHAST -> item = new SkullBuilder(new CachedPlayer("MHF_Ghast"));
            case IRON_GOLEM -> item = new SkullBuilder(new CachedPlayer("MHF_Golem"));
            case MAGMA_CUBE -> item = new SkullBuilder(new CachedPlayer("MHF_LavaSlime"));
            case MOOSHROOM -> item = new SkullBuilder(new CachedPlayer("MHF_MushroomCow"));
            case PIG -> item = new SkullBuilder(new CachedPlayer("MHF_Pig"));
            case SLIME -> item = new SkullBuilder(new CachedPlayer("MHF_Slime"));
            case SPIDER -> item = new SkullBuilder(new CachedPlayer("MHF_Spider"));
            case SQUID -> item = new SkullBuilder(new CachedPlayer("MHF_Squid"));
            case VILLAGER -> item = new SkullBuilder(new CachedPlayer("MHF_Villager"));
            case WITHER_SKELETON -> item = new SkullBuilder(new CachedPlayer("MHF_WSkeleton"));
        }
        return item;
    }

}
