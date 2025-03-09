package me.itsmcb.vexelcore.bukkit.api.cache.exceptions;

import java.net.URL;
import java.util.UUID;

public class PlayerNotFoundException extends Exception {

    public PlayerNotFoundException() {}

    public PlayerNotFoundException(URL url) {
        super("Could not find player for URL request "+url.toString());
    }

    public PlayerNotFoundException(String value) {
        super("Could not find player with the lookup value: "+value);
    }

    public PlayerNotFoundException(UUID value) {
        super("Could not find player with the UUID: "+value);
    }
}