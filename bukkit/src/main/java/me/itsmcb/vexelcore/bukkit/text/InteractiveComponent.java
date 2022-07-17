package me.itsmcb.vexelcore.bukkit.text;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsmcb.vexelcore.bukkit.api.utils.HookUtils;
import me.itsmcb.vexelcore.bukkit.api.utils.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class InteractiveComponent {

    private String mainText;
    private String hoverText;
    private ClickEvent.Action clickEvent;
    private String eventValue;
    private boolean isPreSetupComponent;
    private ArrayList<Component> preSetupComponents;

    // TODO Add all possible ACTION options
    public InteractiveComponent(String mainText, String hoverText, ClickEvent.Action clickEvent, String eventValue) {
        this.mainText = mainText;
        this.hoverText = hoverText;
        this.clickEvent = clickEvent;
        this.eventValue = eventValue;
        this.isPreSetupComponent = false;
        replaceNullTextWithEmpty();
    }

    public InteractiveComponent(ArrayList<Component> components) {
        this.isPreSetupComponent = true;
        this.preSetupComponents = components;
        replaceNullTextWithEmpty();
    }

    private void replaceNullTextWithEmpty() {
        if (this.mainText == null) {
            this.mainText = "";
        }
        if (this.hoverText == null) {
            this.hoverText = "";
        }
        if (this.eventValue == null) {
            this.eventValue = "";
        }
    }

    public boolean parseWithPlaceholderAPI(Player player) {
        if (HookUtils.pluginIsLoaded("PlaceholderAPI")) {
            mainText = PlaceholderAPI.setPlaceholders(player, mainText);
            hoverText = PlaceholderAPI.setPlaceholders(player, hoverText);
            eventValue = PlaceholderAPI.setPlaceholders(player, eventValue);
            System.out.println("Main text: " + mainText);
            return true;
        }
        return false;
    }

    public void replaceText(String replaceRegex, String replacement) {
        mainText = mainText.replaceAll(replaceRegex, replacement);
        hoverText = hoverText.replaceAll(replaceRegex, replacement);
        eventValue = eventValue.replaceAll(replaceRegex, replacement);
    }

    public Component get() {
        if (isPreSetupComponent) {
            return Msg.sendOneLine(preSetupComponents);
        }
        if (clickEvent == null) {
            return Msg.componentize(mainText);
        }
        // TODO if hoverText is null then don't add component
        switch (clickEvent) {
            case RUN_COMMAND -> {
                return Msg.runCommand(mainText, hoverText, eventValue);
            }
            case SUGGEST_COMMAND -> {
                return Msg.suggestCommand(mainText, hoverText, eventValue);
            }
            case OPEN_URL -> {
                return Msg.openWebsite(mainText, hoverText, eventValue);
            }
            case COPY_TO_CLIPBOARD -> {
                return Msg.copyContent(mainText, hoverText, eventValue);
            }
        }
        return null;
    }

    public String getMainText() {
        if (isPreSetupComponent) {
            // KEEP "" TO AVOID NULL ERRORS
            StringBuilder stringBuilder = new StringBuilder();
            for (Component preSetupComponent : preSetupComponents) {
                stringBuilder.append(((TextComponent) preSetupComponent).content());
            }
            return stringBuilder.toString();
        }
        return mainText;
    }
}
