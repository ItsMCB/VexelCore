package me.itsmcb.vexelcore.bukkit.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KyoriBuilder  {

    // TODO WIP?

    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");
    private String message;

    public KyoriBuilder(String message) {
        this.message = message;
    }

    public KyoriBuilder colorize() {
        Matcher matcher = COLOR_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            NamedTextColor color = NamedTextColor.NAMES.value(matcher.group(1));
            matcher.appendReplacement(sb, "");
            sb.append(Component.text("", color));
        }
        matcher.appendTail(sb);
        message = sb.toString();
        return this;
    }

    public Component build() {
        return Component.text(message);
    }

}
