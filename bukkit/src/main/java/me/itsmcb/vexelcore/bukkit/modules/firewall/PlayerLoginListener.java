package me.itsmcb.vexelcore.bukkit.modules.firewall;

import me.itsmcb.vexelcore.api.utils.WebRequest;
import me.itsmcb.vexelcore.api.utils.WebRequestResponse;
import me.itsmcb.vexelcore.bukkit.api.utils.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.InetAddress;
import java.util.UUID;

public class PlayerLoginListener implements Listener {

    // TODO Get user agent from config
    private static final String commonUserAgent = "SouthHollow/0.1";
    private static final String kickMessageInvalid = "&cSomething seems off with your connection... Please try again later.\n\n&7ERR:484";
    private static final String kickMessageError = "&cUnable to authenticate at this time.";

    private boolean validCombo = false;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        InetAddress raw_address = event.getAddress();
        String address = raw_address.toString();
        String hostname = event.getHostname();
        Player player = event.getPlayer();
        UUID rawUUID = player.getUniqueId();

        try {
            WebRequest webRequest = new WebRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + rawUUID.toString().replace("-",""));
            webRequest.setUserAgent(commonUserAgent);
            WebRequestResponse webRequestResponse = webRequest.getWebRequestResponse();
            if (webRequestResponse.getResponseCode() == 200) {
                try {
                    JSONParser parser = new JSONParser();
                    Object resultObject = parser.parse(webRequestResponse.getWebsiteData());
                    if (resultObject instanceof JSONObject jsonObject) {
                        String resolvedName = jsonObject.get("name").toString();
                        if (!resolvedName.equals(player.getName())) {
                           player.kick(BukkitUtils.componentize(kickMessageInvalid));
                        } else {
                            validCombo = true;
                        }
                    } else {
                        // TODO log error: Couldn't parse JSON object
                        player.kick(BukkitUtils.componentize(kickMessageError));
                    }
                } catch (ParseException parseException) {
                    // TODO log error: Parse exception
                    parseException.printStackTrace();
                    player.kick(BukkitUtils.componentize(kickMessageError));
                }
            } else {
                // TODO Log error: Expected response 200 but got...
                player.kick(BukkitUtils.componentize(kickMessageError));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info("[Firewall] -> " + player.getName() + " | validCombo = " + validCombo + " | Hostname: " + hostname + " | Address: + " + address);

    }
}
