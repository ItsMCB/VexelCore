package me.itsmcb.vexelcore.bukkit.api.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import org.bukkit.entity.Player;

public class DialogContext {
    private final Player player;
    private final DialogResponseView view;

    public DialogContext(Player player, DialogResponseView view) {
        this.player = player;
        this.view = view;
    }

    public Player getPlayer() {
        return player;
    }

    public String getInputText(String inputId) {
        if (view == null) return null;
        return view.getText(inputId);
    }

    public int getInputInt(String inputId, int fallback) {
        if (view == null) return fallback;
        Float f = view.getFloat(inputId);
        return f != null ? f.intValue() : fallback;
    }

    public float getInputFloat(String inputId, float fallback) {
        if (view == null) return fallback;
        Float f = view.getFloat(inputId);
        return f != null ? f : fallback;
    }

    public boolean getInputBool(String inputId) {
        if (view == null) return false;
        Boolean b = view.getBoolean(inputId);
        return b != null && b;
    }
}