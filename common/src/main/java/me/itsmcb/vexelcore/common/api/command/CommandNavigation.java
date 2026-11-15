package me.itsmcb.vexelcore.common.api.command;

import me.itsmcb.vexelcore.common.api.text.Icon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;

/**
 * Standardized text-based pagination footer for chat commands across platforms.
 *
 * <p>Renders clickable {@code « Prev} and {@code Next »} navigation controls
 * around a {@code Page X of Y} indicator using Adventure components, omitting
 * the footer entirely when there is only one page.</p>
 */
public final class CommandNavigation {

    private CommandNavigation() {
    }

    /**
     * Builds the pagination footer component for commands whose target page is
     * appended as the trailing argument (e.g., {@code "/v help"} or {@code "/history Steve"}).
     *
     * @param commandPrefix Base command without the page number (leading slash optional).
     * @param currentPage   Current 1-based page number.
     * @param maxPages      Total number of pages.
     * @return The built footer component, or empty when {@code maxPages <= 1}.
     */
    public static @NotNull Optional<Component> create(@NotNull String commandPrefix, int currentPage, int maxPages) {
        String prefix = Objects.requireNonNull(commandPrefix, "commandPrefix").trim();
        return create(currentPage, maxPages, page -> prefix + " " + page);
    }

    /**
     * Builds the pagination footer component using a custom command formatter for
     * each target page number.
     *
     * @param currentPage        Current 1-based page number.
     * @param maxPages           Total number of pages.
     * @param pageCommandBuilder Function mapping a 1-based target page to the command to execute.
     * @return The built footer component, or empty when {@code maxPages <= 1}.
     */
    public static @NotNull Optional<Component> create(int currentPage, int maxPages, @NotNull IntFunction<String> pageCommandBuilder) {
        Objects.requireNonNull(pageCommandBuilder, "pageCommandBuilder");
        if (maxPages <= 1) {
            return Optional.empty();
        }

        int actualPage = Math.max(1, Math.min(currentPage, maxPages));
        TextComponent.Builder builder = Component.text();

        if (actualPage > 1) {
            int prevPage = actualPage - 1;
            String prevCmd = normalizeCommand(pageCommandBuilder.apply(prevPage));
            Component prevHover = Component.text()
                    .append(Component.text("Click to view page ", NamedTextColor.GRAY))
                    .append(Component.text(prevPage, NamedTextColor.YELLOW))
                    .build();

            builder.append(Component.text(Icon.DOUBLE_ARROW_LEFT.getIcon() + " Prev", NamedTextColor.AQUA)
                    .hoverEvent(HoverEvent.showText(prevHover))
                    .clickEvent(ClickEvent.runCommand(prevCmd)));
            builder.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
        }

        builder.append(Component.text("Page ", NamedTextColor.GRAY))
                .append(Component.text(actualPage, NamedTextColor.YELLOW))
                .append(Component.text(" of ", NamedTextColor.GRAY))
                .append(Component.text(maxPages, NamedTextColor.YELLOW));

        if (actualPage < maxPages) {
            int nextPage = actualPage + 1;
            String nextCmd = normalizeCommand(pageCommandBuilder.apply(nextPage));
            Component nextHover = Component.text()
                    .append(Component.text("Click to view page ", NamedTextColor.GRAY))
                    .append(Component.text(nextPage, NamedTextColor.YELLOW))
                    .build();

            builder.append(Component.text(" | ", NamedTextColor.DARK_GRAY));
            builder.append(Component.text("Next " + Icon.DOUBLE_ARROW_RIGHT.getIcon(), NamedTextColor.AQUA)
                    .hoverEvent(HoverEvent.showText(nextHover))
                    .clickEvent(ClickEvent.runCommand(nextCmd)));
        }

        return Optional.of(builder.build());
    }

    /**
     * Sends the pagination footer to the given audience when {@code maxPages > 1}.
     *
     * @param audience      Recipient audience (Paper {@code CommandSender}, Velocity {@code CommandSource}, etc.).
     * @param commandPrefix Base command without the page number (leading slash optional).
     * @param currentPage   Current 1-based page number.
     * @param maxPages      Total number of pages.
     */
    public static void send(@NotNull Audience audience, @NotNull String commandPrefix, int currentPage, int maxPages) {
        Objects.requireNonNull(audience, "audience");
        create(commandPrefix, currentPage, maxPages).ifPresent(audience::sendMessage);
    }

    /**
     * Sends the pagination footer to the given audience using a custom command formatter
     * when {@code maxPages > 1}.
     *
     * @param audience           Recipient audience (Paper {@code CommandSender}, Velocity {@code CommandSource}, etc.).
     * @param currentPage        Current 1-based page number.
     * @param maxPages           Total number of pages.
     * @param pageCommandBuilder Function mapping a 1-based target page to the command to execute.
     */
    public static void send(@NotNull Audience audience, int currentPage, int maxPages, @NotNull IntFunction<String> pageCommandBuilder) {
        Objects.requireNonNull(audience, "audience");
        create(currentPage, maxPages, pageCommandBuilder).ifPresent(audience::sendMessage);
    }

    private static @NotNull String normalizeCommand(@NotNull String command) {
        String trimmed = Objects.requireNonNull(command, "command").trim();
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }
}