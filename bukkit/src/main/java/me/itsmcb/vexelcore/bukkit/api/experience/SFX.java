package me.itsmcb.vexelcore.bukkit.api.experience;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public enum SFX {
    POP(Sound.sound(Key.key("block.lava.pop"), Sound.Source.MASTER, 1F, 0.8F)),
    PICKUP(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.MASTER, 1F, 0.8F)),
    PICKUP_HIGH(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.MASTER, 1F, 1.8F));

    private Sound sound;

    SFX(Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return sound;
    }
}
