package me.itsmcb.vexelcore.bukkit.api.framework;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A container node that holds child {@link VexelCommandNode}s.
 *
 * <p>This is the key abstraction that enables composability. A group can contain
 * both {@link VexelSubCommand}s (leaf commands) and other {@code VexelCommandGroup}s
 * (including {@link VexelRootCommand}s from other plugins).</p>
 *
 * <p>Example: attaching Voyage's root command as a child of LibreProperty's root:</p>
 * <pre>{@code
 * public class LPCommand extends VexelRootCommand<LPContext> {
 *     private final VexelRootCommand<VoyageContext> voyageCmd;
 *
 *     public LPCommand(LPContext ctx, VexelRootCommand<VoyageContext> voyageCmd) {
 *         super(ctx, "lp", "LibreProperty", "lp.use");
 *         this.voyageCmd = voyageCmd;
 *     }
 *
 *     @Override
 *     public @NotNull List<VexelCommandNode> getChildren() {
 *         return List.of(
 *             new CreatePropertyCmd(context),
 *             voyageCmd  // ← /lp v create, /lp v delete, etc. all work!
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p>When executed without a child match, the group shows paginated help.</p>
 *
 * @param <C> The context type.
 */
public abstract class VexelCommandGroup<C extends VexelContext> implements VexelCommandNode {

    private static final int DEFAULT_ITEMS_PER_PAGE = 6;

    protected final C context;

    private final String name;
    private final String description;
    private final String permission;

    /**
     * Creates a command group with the given metadata.
     *
     * @param context     The plugin context.
     * @param name        The literal name of this group.
     * @param description The description (supports MiniMessage).
     * @param permission  The required permission.
     */
    public VexelCommandGroup(@NotNull C context, @NotNull String name, @NotNull String description, @NotNull String permission) {
        this.context = context;
        this.name = name;
        this.description = description;
        this.permission = permission;
    }

    // ── Metadata ─────────────────────────────────────────────────────────

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }

    @Override
    public @NotNull String getPermission() {
        return permission;
    }

    /**
     * Returns the child nodes of this group.
     * Override to provide sub-commands and nested groups.
     *
     * @return A list of child nodes.
     */
    @Override
    public abstract @NotNull List<VexelCommandNode> getChildren();

    // ── Configuration hooks ──────────────────────────────────────────────

    /**
     * Returns the number of items to show per help page.
     * Override to customize.
     *
     * @return Items per page.
     */
    protected int itemsPerPage() {
        return DEFAULT_ITEMS_PER_PAGE;
    }

    /**
     * Whether to automatically register a {@code help} sub-command.
     * Override to disable.
     *
     * @return {@code true} to add the help branch (default).
     */
    protected boolean hasAutoHelp() {
        return true;
    }

    // ── Execution (default: show help) ───────────────────────────────────

    @Override
    public int execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull VexelCommandPath path) throws CommandSyntaxException {
        List<VexelCommandNode> children = getChildren();
        if (children.isEmpty()) {
            TextUtils.send(ctx.getSource().getSender(),
                    "<red>This command has no sub-commands.</red>");
        } else {
            sendPaginatedHelp(ctx.getSource(), path, 1);
        }
        return Command.SINGLE_SUCCESS;
    }

    // ── Brigadier building ───────────────────────────────────────────────

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> buildBrigadier(@NotNull VexelCommandPath parentPath) {
        return buildBrigadier(parentPath, getName());
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> buildBrigadier(@NotNull VexelCommandPath parentPath, @NotNull String literalName) {
        VexelCommandPath myPath = parentPath.child(literalName);

        var literal = Commands.literal(literalName)
                .requires(this::hasPermission)
                .executes(ctx -> execute(ctx, myPath));

        List<VexelCommandNode> children = getChildren();

        if (!children.isEmpty()) {
            // Add help branch (unless a child is already named "help")
            boolean hasHelpChild = children.stream().anyMatch(c ->
                    c.getName().equals("help") || c.getAliases().contains("help"));

            if (hasAutoHelp() && !hasHelpChild) {
                var helpBuilder = Commands.literal("help")
                        .requires(this::hasPermission)
                        .executes(ctx -> {
                            sendPaginatedHelp(ctx.getSource(), myPath, 1);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    int page = ctx.getArgument("page", Integer.class);
                                    sendPaginatedHelp(ctx.getSource(), myPath, page);
                                    return Command.SINGLE_SUCCESS;
                                }));
                literal.then(helpBuilder);
            }

            // Register children and their aliases as full sibling trees
            for (VexelCommandNode child : children) {
                literal.then(child.buildBrigadier(myPath));

                Set<String> registered = new HashSet<>();
                registered.add(child.getName().toLowerCase(Locale.ROOT));

                for (String alias : child.getAliases()) {
                    if (registered.add(alias.toLowerCase(Locale.ROOT))) {
                        literal.then(child.buildBrigadier(myPath, alias));
                    }
                }
            }
        }

        return literal;
    }

    // ── Help ─────────────────────────────────────────────────────────────

    /**
     * Sends paginated help listing all visible children.
     *
     * @param source The command source stack.
     * @param path   The full path to this group.
     * @param page   The page number (1-based).
     */
    protected void sendPaginatedHelp(@NotNull CommandSourceStack source, @NotNull VexelCommandPath path, int page) {
        // Filter by permission FIRST so pagination counts are accurate
        List<VexelCommandNode> visible = getChildren().stream()
                .filter(child -> child.hasPermission(source))
                .collect(Collectors.toList());

        if (visible.isEmpty()) {
            TextUtils.send(source.getSender(),
                    "<gray>No commands available.</gray>");
            return;
        }

        int perPage = itemsPerPage();
        int totalItems = visible.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalItems / perPage));
        int actualPage = Math.max(1, Math.min(page, maxPages));

        int startIndex = (actualPage - 1) * perPage;
        int endIndex = Math.min(startIndex + perPage, totalItems);

        String cmd = path.asCommand();

        if (maxPages > 1) {
            TextUtils.send(source.getSender(),
                    "<dark_gray><st>          </st> <color:#e98df5>\ud83d\udcc4 Command Help: <green><cmd> help [page]</green></color> <yellow>(<page>/<max>)</yellow> <st>          </st> </dark_gray>",
                    Placeholder.unparsed("cmd", cmd),
                    Placeholder.unparsed("page", String.valueOf(actualPage)),
                    Placeholder.unparsed("max", String.valueOf(maxPages))
            );
        } else {
            TextUtils.send(source.getSender(),
                    "<dark_gray><st>          </st> <color:#e98df5>\ud83d\udcc4 Command Help: <green><cmd></green></color> <st>          </st> </dark_gray>",
                    Placeholder.unparsed("cmd", cmd)
            );
        }

        for (VexelCommandNode child : visible.subList(startIndex, endIndex)) {
            String suggest = cmd + " " + child.getName() + " ";

            // Show ▸ for groups (have children), - for leaf commands
            boolean isGroup = !child.getChildren().isEmpty();
            Component icon = Component.text(isGroup ? " ▸ " : " - ")
                    .color(net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY);

            TextUtils.send(source.getSender(),
                    "<click:suggest_command:'" + suggest + "'><hover:show_text:'<gray>Click to start: </gray><white>" + suggest + "</white>'><green><cmd> <name></green></hover></click><icon><gray><desc></gray>",
                    Placeholder.unparsed("cmd", cmd),
                    Placeholder.unparsed("name", child.getName()),
                    Placeholder.parsed("desc", child.getDescription()),
                    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver("icon", net.kyori.adventure.text.minimessage.tag.Tag.inserting(icon))
            );
        }

        if (maxPages > 1) {
            int prevPage = actualPage - 1;
            int nextPage = actualPage + 1;

            StringBuilder nav = new StringBuilder();

            if (actualPage > 1) {
                nav.append("<click:run_command:'").append(cmd).append(" help ").append(prevPage)
                        .append("'><hover:show_text:'<gray>Click to view page </gray><yellow>").append(prevPage)
                        .append("</yellow>'><aqua>« Prev</aqua></hover></click><dark_gray> | </dark_gray>");
            }

            nav.append("<gray>Page </gray><yellow><page></yellow><gray> of </gray><yellow><max></yellow>");

            if (actualPage < maxPages) {
                nav.append("<dark_gray> | </dark_gray><click:run_command:'").append(cmd).append(" help ").append(nextPage)
                        .append("'><hover:show_text:'<gray>Click to view page </gray><yellow>").append(nextPage)
                        .append("</yellow>'><aqua>Next »</aqua></hover></click>");
            }

            TextUtils.send(source.getSender(),
                    nav.toString(),
                    Placeholder.unparsed("page", String.valueOf(actualPage)),
                    Placeholder.unparsed("max", String.valueOf(maxPages))
            );
        }
    }
}