package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class ItemData {
    private NamespacedKey namespacedKey;
    private PersistentDataType persistentDataType;
    private Object data;

    public ItemData(NamespacedKey key, String str) {
        this.namespacedKey = key;
        this.persistentDataType = PersistentDataType.STRING;
        this.data = str;
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    public PersistentDataType getPersistentDataType() {
        return persistentDataType;
    }

    public Object getData() {
        return data;
    }
}
