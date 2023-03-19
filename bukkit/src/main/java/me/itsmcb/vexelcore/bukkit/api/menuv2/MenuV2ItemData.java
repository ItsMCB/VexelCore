package me.itsmcb.vexelcore.bukkit.api.menuv2;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class MenuV2ItemData {

    private NamespacedKey key;
    private PersistentDataType type;

    private String string;

    // String
    public MenuV2ItemData(NamespacedKey key, String value) {
        this.key = key;
        this.type = PersistentDataType.STRING;
        this.string = value;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public PersistentDataType getType() {
        return type;
    }

    public String getString() {
        return string;
    }
}
