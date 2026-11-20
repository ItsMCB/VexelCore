package me.itsmcb.vexelcore.bukkit.api.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsmcb.vexelcore.bukkit.api.cache.CachedPlayerV2;
import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.common.api.HeadTexture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtils {

    public static final Key ATLAS_ITEMS = Key.key("minecraft:items");
    public static final Key ATLAS_BLOCKS = Key.key("minecraft:blocks");
    public static final Key ATLAS_GUI = Key.key("minecraft:gui");
    public static final Key ATLAS_MOB_EFFECTS = Key.key("minecraft:mob_effects");
    public static final Key ATLAS_PAINTINGS = Key.key("minecraft:paintings");
    public static final Key ATLAS_PARTICLES = Key.key("minecraft:particles");
    public static final Key ATLAS_MAP_DECORATIONS = Key.key("minecraft:map_decorations");

    /**
     * Creates a LegacyComponentSerializer that supports both
     * legacy color codes (e.g., "&a") and hexadecimal color codes (e.g., "&#803ceb").
     *
     * @return A new LegacyComponentSerializer instance.
     */
    public static LegacyComponentSerializer getColorizer() {
        return LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build();
    }

    /**
     * Apply the & and hex color codes from the component text content to the component
     * @param component Component to transform
     * @return Component with in-text color codes applied
     */
    public static Component colorizeComponentText(@NotNull Component component) {
        return getColorizer().deserialize(
                getColorizer().serialize(component)
        );
    }

    /**
     * Applies PlaceholderAPI placeholders to the given text if PlaceholderAPI is loaded.
     *
     * @param player The player context for placeholders (may be null).
     * @param text   The input text.
     * @return The text with placeholders resolved, or the original text if PAPI is not loaded.
     */
    public static @Nullable String applyPlaceholders(@Nullable OfflinePlayer player, @Nullable String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return PluginUtils.pluginIsLoaded("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, text)
                : text;
    }

    /**
     * Recursively flattens a component and its children into a single string.
     * Only {@link TextComponent} are processed; other component types are ignored.
     *
     * @param component The component to flatten. Can be null.
     * @return A string representation of the component's text content and the text content of its children,
     * or an empty string if the component is not a {@link TextComponent}.
     */
    public static String flattenComponent(Component component) {
        if (component instanceof TextComponent textComponent) { // Reminder: Handles null case
            StringBuilder builder = new StringBuilder(textComponent.content());
            List<Component> children = textComponent.children();
            for (Component child : children) {
                builder.append(flattenComponent(child));
            }
            return builder.toString();
        } else {
            return "";
        }
    }

    public static List<Audience> removePlayerAudience(@NotNull Set<Audience> viewers) {
        List<Audience> playerAudienceList = new ArrayList<>();
        viewers.stream()
                .filter(member -> member instanceof Player)
                .forEach(playerAudienceList::add);
        playerAudienceList.forEach(viewers::remove);
        return playerAudienceList;
    }

    public static String hexToColorCode(String hexValue) {
        StringBuilder formattedString = new StringBuilder("&x");
        // Remove the leading '#' if present
        if (hexValue.startsWith("#")) {
            hexValue = hexValue.substring(1);
        }
        // Iterate through each character in the hex value
        for (char c : hexValue.toCharArray()) {
            formattedString.append('&').append(c);
        }
        return formattedString.toString();
    }

    /**
     * Pattern to find any color (including hex) or format code.
     * Group 1: The prefix (& or §)
     * Group 2: The actual code sequence (e.g., "c", "l", or "#7f3bec"). Case-insensitivity via (?i).
     */
    private static final Pattern FORMAT_CODE_PATTERN =
            Pattern.compile("(?i)([&§])(#[0-9A-F]{6}|[0-9A-FK-ORL])");

    /**
     * Pattern to strip all known color and format codes, including Bukkit '&x&R&R&G&G&B&B' hex sequences.
     * The trailing '+' makes it match consecutive codes (e.g., "&c&l") as a single unit for replacement.
     */
    private static final Pattern STRIP_CODES_PATTERN =
            Pattern.compile("(?i)(?:[&§]x(?:[&§][0-9A-F]){6}|[&§](?:#[0-9A-F]{6}|[0-9A-FK-ORL]))+");

    // Character sets
    private static final Set<Character> LEGACY_COLOR_CHARS = "0123456789abcdef".chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toUnmodifiableSet());
    private static final Set<Character> LEGACY_FORMAT_CHARS = "klmno".chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toUnmodifiableSet());
    private static final char LEGACY_RESET_CHAR_LOWER = 'r';

    private static final int DEFAULT_SPLIT_LENGTH = 50;

    /**
     * Standard serializer for '&' based legacy text
     */
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Removes all known color (including hex) and formatting codes from a string.
     * Ex. "&cHello &#7f3bec&lWorld" → "Hello World"
     *
     * @param input The string to remove codes from.
     * @return The string with codes removed or an empty string if input is null or empty.
     */
    public static String stripFormattingCodes(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return STRIP_CODES_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Represents the current formatting state (last color, active formats).
     * This class is mutable and designed to be updated as text segments are processed.
     */
    private static class FormattingState {
        String lastColorOrHex = ""; // Current color as full code (e.g., "&c" or "&#7f3bec")
        Set<Character> activeLegacyFormats = new LinkedHashSet<>(); // Active formats (bold, italic, etc.)
        char currentPrefixChar = '&'; // Default prefix, updated by the last encountered code prefix.

        /**
         * Resets the color and formatting to default (empty) while preserving the last prefix character.
         */
        void reset() {
            lastColorOrHex = "";
            activeLegacyFormats.clear();
        }

        /**
         * Applies format codes found in the given text segment to this state.
         *
         * @param textSegment The text segment to parse for codes.
         */
        void applyCodesFrom(String textSegment) {
            if (textSegment == null || textSegment.isEmpty()) {
                return;
            }

            var matcher = FORMAT_CODE_PATTERN.matcher(textSegment);
            while (matcher.find()) {
                this.currentPrefixChar = matcher.group(1).charAt(0); // The '&' or '§'
                String codeSequence = matcher.group(2); // The code itself, e.g., "c" or "#7f3bec"

                if (codeSequence.length() == 1) { // Legacy single-character code
                    char codeChar = Character.toLowerCase(codeSequence.charAt(0));
                    if (LEGACY_COLOR_CHARS.contains(codeChar)) {
                        lastColorOrHex = this.currentPrefixChar + String.valueOf(codeChar);
                        activeLegacyFormats.clear(); // Color codes reset active formats
                    } else if (LEGACY_FORMAT_CHARS.contains(codeChar)) {
                        activeLegacyFormats.add(codeChar); // Add to set of active formats
                    } else if (codeChar == LEGACY_RESET_CHAR_LOWER) {
                        reset(); // Reset all formatting
                    }
                    // Other single characters (e.g. 'x' not part of a full hex sequence) are ignored.
                } else if (codeSequence.startsWith("#")) { // Hex color format: #RRGGBB
                    lastColorOrHex = this.currentPrefixChar + codeSequence; // Store full hex: &#7f3bec
                    activeLegacyFormats.clear(); // Hex colors also reset active formats
                }
            }
        }

        /**
         * Converts the current formatting state to its string representation (e.g., "&c&l&o" or "&#7f3bec&l").
         *
         * @return A string of active color and format codes.
         */
        @Override
        public String toString() {
            var sb = new StringBuilder(lastColorOrHex);
            for (char formatChar : activeLegacyFormats) {
                // Format codes are typically represented in lowercase.
                sb.append(currentPrefixChar).append(Character.toLowerCase(formatChar));
            }
            return sb.toString();
        }
    }

    /**
     * Extracts the last active color and formatting codes from a string.
     * Useful for continuing formatting on a new line or segment.
     *
     * @param input The string to extract codes from. Null input results in an empty string.
     * @return A string containing the last active codes (e.g., "&c&l" or "&#7f3bec&o").
     *         Returns an empty string if no codes are active or input is null/empty.
     */
    public static String getLastFormattingCodes(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        var state = new FormattingState();
        state.applyCodesFrom(input);
        return state.toString();
    }

    /**
     * Splits a colorized string into multiple lines, each not exceeding {@code maxLength}
     * visible characters, while preserving color and formatting across line breaks.
     * Words longer than {@code maxLength} will not be broken and will occupy their own line.
     *
     * @param text The colorized text to split. Must not be null.
     * @return A list of {@link TextComponent}s, each representing a formatted line.
     */
    public static List<TextComponent> splitText(@NotNull String text) {
        return splitText(text, DEFAULT_SPLIT_LENGTH);
    }

    /**
     * Splits a colorized string into multiple lines, each not exceeding {@code maxLength}
     * visible characters, while preserving color and formatting across line breaks.
     * Words longer than {@code maxLength} will not be broken and will occupy their own line.
     *
     * @param text The colorized text to split. Must not be null.
     * @param maxLength The maximum number of visible (non-code) characters per line.
     * @return A list of {@link TextComponent}s, each representing a formatted line.
     * @throws IllegalArgumentException if maxLength is not positive.
     */
    public static List<TextComponent> splitText(@NotNull String text, int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be positive and greater than zero.");
        }

        var resultLines = new ArrayList<TextComponent>();
        if (text.isEmpty()) {
            return resultLines; // No lines for completely empty text.
        }

        // If text is effectively empty (only codes and/or whitespace), add it as one component.
        if (stripFormattingCodes(text).trim().isEmpty()) {
            resultLines.add(AMPERSAND_SERIALIZER.deserialize(text));
            return resultLines;
        }

        var currentLineBuilder = new StringBuilder();
        // activeFormatting tracks the codes that are active at the end of the currently built line segment.
        // This state is used to prefix the next line if a break occurs.
        var activeFormatting = new FormattingState();
        int visibleCharsOnCurrentLine = 0;

        // Split by space. The -1 limit preserves trailing empty strings, necessary for multiple spaces.
        String[] words = text.split(" ", -1);

        for (String word : words) {
            String strippedWord = stripFormattingCodes(word);
            int wordVisibleLength = strippedWord.length();

            // Determine if a space is needed before this word.
            // A space is needed if currentLineBuilder already has visible content.
            boolean needsSpace = visibleCharsOnCurrentLine > 0;
            int spaceLength = needsSpace ? 1 : 0;

            // Check for line overflow:
            // If adding the current word (plus a space if needed) would exceed maxLength,
            // AND the current line already has some visible characters (to avoid creating empty initial lines).
            if (visibleCharsOnCurrentLine > 0 && (visibleCharsOnCurrentLine + spaceLength + wordVisibleLength > maxLength)) {
                resultLines.add(AMPERSAND_SERIALIZER.deserialize(currentLineBuilder.toString()));
                currentLineBuilder.setLength(0);
                // Start new line with the carried-over formatting.
                // activeFormatting reflects the state *after* the last fully processed word on the previous line.
                currentLineBuilder.append(activeFormatting.toString());
                visibleCharsOnCurrentLine = 0;
                needsSpace = false; // No leading space on a brand-new line.
            }

            // Add space if it's not the first word on this current line construction and there was visible content.
            if (needsSpace) {
                currentLineBuilder.append(" ");
                visibleCharsOnCurrentLine++;
            }

            currentLineBuilder.append(word);
            activeFormatting.applyCodesFrom(word); // Update active formatting state based on codes from this word.
            visibleCharsOnCurrentLine += wordVisibleLength;
        }

        // Add any remaining text in currentLineBuilder as the last line.
        // This is safe because the initial `stripFormattingCodes(text).trim().isEmpty()` check
        // handles cases where the *entire* input `text` is visually empty.
        // If currentLineBuilder has content here, it's either part of visible text or a continuation.
        if (!currentLineBuilder.isEmpty()) {
            resultLines.add(AMPERSAND_SERIALIZER.deserialize(currentLineBuilder.toString()));
        }

        return resultLines;
    }

    /**
     * Creates a Component containing a Player Head.
     * <br>
     * <a href="https://minecraft.wiki/w/Text_component_format#Player_Object_Type">Minecraft Wiki Documentation about Player_Object_Type Text Component</a>
     */
    public static Component getHeadComponent(OfflinePlayer player) {
        return getHeadComponent(player.getPlayerProfile().getTextures().getSkin());
    }

    /**
     * Creates a Component containing a Player Head.
     * <br>
     * <a href="https://minecraft.wiki/w/Text_component_format#Player_Object_Type">Minecraft Wiki Documentation about Player_Object_Type Text Component</a>
     */
    public static Component getHeadComponent(CachedPlayerV2 p) {
        return getHeadComponent(p.getPlayerSkinData().getSkinUrl());
    }

    /**
     * Creates a Component containing a Player Head.
     * <br>
     * <a href="https://minecraft.wiki/w/Text_component_format#Player_Object_Type">Minecraft Wiki Documentation about Player_Object_Type Text Component</a>
     */
    public static Component getHeadComponent(HeadTexture ht) {
        return Component.object(getPlayerHeadObjectContents(ht.getTexture()));
    }

    /**
     * Creates a Component containing a Player Head.
     * <br>
     * <a href="https://minecraft.wiki/w/Text_component_format#Player_Object_Type">Minecraft Wiki Documentation about Player_Object_Type Text Component</a>
     */
    public static Component getHeadComponent(String skinURL) {
        if (skinURL == null || skinURL.isEmpty()) {
            return Component.text("?VexelCoreAPI Invalid Error?");
        }
        try {
            return getHeadComponent(new URL(skinURL));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a Component containing a Player Head based on a URL.
     * <br>
     * Crafts a JSON payload, converts it to Base64, and applies it to the head's profileProperty.
     * <br>
     * <a href="https://minecraft.wiki/w/Text_component_format#Player_Object_Type">Minecraft Wiki Documentation about Player_Object_Type Text Component</a>
     */
    public static Component getHeadComponent(URL skinUrl) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + skinUrl + "\"}}}";
        String textureBase64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        return Component.object(getPlayerHeadObjectContents(textureBase64));
    }

    /**
     * Crafts the {@code ObjectContents} with the player head data.
     */
    public static PlayerHeadObjectContents getPlayerHeadObjectContents(String textureBase64) {
        return ObjectContents.playerHead()
                .profileProperty(
                        PlayerHeadObjectContents.property("textures", textureBase64)
                )
                .hat(true)
                .build();
    }

    /**
     * Produces a component showing the Base64 player head to the left and the text to the right.
     */
    public static @NotNull Component makeHeadLabel(@NotNull String textureBase64, @NotNull String text) {
        return Component.empty()
                .append(Component.object(getPlayerHeadObjectContents(textureBase64)))
                .append(new BukkitMsgBuilder(" " + text).get());
    }

    /**
     * Produces a component showing a cached player's head to the left and the text to the right.
     */
    public static @NotNull Component makeLabel(@NotNull CachedPlayerV2 cachedPlayer, @NotNull String text) {
        return makeHeadLabel(cachedPlayer.getPlayerSkinData().getTexture(), text);
    }

    /**
     * Produces a component showing the head to the left and the text to the right.
     */
    public static Component makeLabel(HeadTexture head, String text) {
        Component icon = ChatUtils.getHeadComponent(head);
        Component label = new BukkitMsgBuilder(" " + text).get();
        // Appended to empty to fix Bedrock component rendering issue with icons
        return Component.empty().append(icon).append(label);
    }

    /**
     * Produces a component showing the head to the left and the component to the right.
     */
    public static Component makeLabel(HeadTexture head, Component component) {
        Component icon = ChatUtils.getHeadComponent(head);
        Component spacer = new BukkitMsgBuilder(" ").get();
        // Appended to empty to fix Bedrock component rendering issue with icons
        return Component.empty().append(icon).append(spacer).append(component);
    }

    // =========================================================================
    // Sprites & Sprite Labels
    // =========================================================================

    public record SpriteDefinition(@NotNull Key atlas, @NotNull Key sprite) {
        public SpriteDefinition(@NotNull Key atlas, @NotNull String spriteKey) {
            this(atlas, Key.key(spriteKey));
        }
    }

    /**
     * Creates a standalone sprite component from an explicit atlas and sprite key.
     */
    public static @NotNull Component getSpriteComponent(@NotNull Key atlas, @NotNull Key sprite) {
        return Component.object(ObjectContents.sprite(atlas, sprite));
    }

    /**
     * Creates a standalone sprite component from a resolved {@link SpriteDefinition}.
     */
    public static @NotNull Component getSpriteComponent(@NotNull SpriteDefinition spriteDef) {
        return getSpriteComponent(spriteDef.atlas(), spriteDef.sprite());
    }

    /**
     * Creates a standalone sprite component, inferring the atlas from the sprite path prefix
     * (e.g., {@code "block/stone"}, {@code "item/diamond"}, {@code "gui/icon/checkmark"}).
     */
    public static @NotNull Component getSpriteComponent(@NotNull String spriteKey) {
        return getSpriteComponent(resolveSprite(spriteKey));
    }

    /**
     * Creates a standalone icon component for any Bukkit {@link Material}.
     * Player head materials are rendered using {@link ObjectContents#playerHead()}.
     */
    public static @NotNull Component getSpriteComponent(@NotNull Material material) {
        if (material == Material.PLAYER_HEAD || material == Material.PLAYER_WALL_HEAD) {
            return Component.object(ObjectContents.playerHead().hat(true).build());
        }
        return getSpriteComponent(resolveSprite(material));
    }

    /**
     * Produces a component showing a sprite from any atlas to the left and text to the right.
     */
    public static @NotNull Component makeLabel(@NotNull Key atlas, @NotNull Key sprite, @NotNull String text) {
        return makeLabel(atlas, sprite, new BukkitMsgBuilder(text).get());
    }

    /**
     * Produces a component showing a sprite from any atlas to the left and a component to the right.
     */
    public static @NotNull Component makeLabel(@NotNull Key atlas, @NotNull Key sprite, @NotNull Component component) {
        // Appended to empty to preserve Bedrock/Geyser icon rendering compatibility
        return Component.empty()
                .append(getSpriteComponent(atlas, sprite))
                .append(Component.space())
                .append(component);
    }

    /**
     * Produces a component showing a sprite to the left and text to the right.
     * Automatically infers the atlas from prefixes such as {@code block/}, {@code item/}, {@code gui/}, etc.
     */
    public static @NotNull Component makeLabel(@NotNull String spriteKey, @NotNull String text) {
        SpriteDefinition def = resolveSprite(spriteKey);
        return makeLabel(def.atlas(), def.sprite(), text);
    }

    /**
     * Produces a component showing a sprite to the left and a component to the right.
     */
    public static @NotNull Component makeLabel(@NotNull String spriteKey, @NotNull Component component) {
        SpriteDefinition def = resolveSprite(spriteKey);
        return makeLabel(def.atlas(), def.sprite(), component);
    }

    /**
     * Produces a standalone icon component for a {@link Material}.
     */
    public static @NotNull Component makeLabel(@NotNull Material material) {
        return getSpriteComponent(material);
    }

    /**
     * Produces a component with a resolved {@link Material} sprite to the left and text to the right.
     * This method resolves whether the material should use the 'blocks' or 'items' atlas,
     * and handles player heads via {@link ObjectContents#playerHead()}.
     *
     * @param material The material to display.
     * @param text The text to append.
     * @return Component with sprite and text.
     */
    public static @NotNull Component makeLabel(@NotNull Material material, @NotNull String text) {
        return makeLabel(material, new BukkitMsgBuilder(text).get());
    }

    /**
     * Produces a component with a resolved {@link Material} sprite to the left and a component to the right.
     */
    public static @NotNull Component makeLabel(@NotNull Material material, @NotNull Component component) {
        // Appended to empty to preserve Bedrock/Geyser icon rendering compatibility
        return Component.empty()
                .append(getSpriteComponent(material))
                .append(Component.space())
                .append(component);
    }

    private static final Set<String> WOOD_TYPES = Set.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "bamboo", "pale_oak", "crimson", "warped"
    );

    private static final Map<String, String> COMPOUND_BLOCK_BASES = Map.ofEntries(
            Map.entry("brick", "bricks"),
            Map.entry("stone_brick", "stone_bricks"),
            Map.entry("mud_brick", "mud_bricks"),
            Map.entry("tuff_brick", "tuff_bricks"),
            Map.entry("resin_brick", "resin_bricks"),
            Map.entry("deepslate_brick", "deepslate_bricks"),
            Map.entry("deepslate_tile", "deepslate_tiles"),
            Map.entry("nether_brick", "nether_bricks"),
            Map.entry("red_nether_brick", "red_nether_bricks"),
            Map.entry("polished_blackstone_brick", "polished_blackstone_bricks"),
            Map.entry("prismarine_brick", "prismarine_bricks"),
            Map.entry("end_stone_brick", "end_stone_bricks"),
            Map.entry("purpur", "purpur_block"),
            Map.entry("quartz", "quartz_block_side"),
            Map.entry("smooth_quartz", "quartz_block_bottom"),
            Map.entry("smooth_sandstone", "sandstone_top"),
            Map.entry("smooth_red_sandstone", "red_sandstone_top")
    );

    /**
     * Explicit overrides for animated textures, non-standard item names, entity-rendered blocks,
     * and multi-face blocks whose texture path does not match {@code block/<key>} or {@code item/<key>}.
     */
    private static final Map<Material, SpriteDefinition> SPRITE_OVERRIDES = Map.ofEntries(
            // Non-rendering air fallbacks
            Map.entry(Material.AIR, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/structure_void")),
            Map.entry(Material.CAVE_AIR, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/structure_void")),
            Map.entry(Material.VOID_AIR, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/structure_void")),

            // Entity-rendered skulls/heads (no block/<key> or item/<key> sprite exists)
            Map.entry(Material.PLAYER_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/armor_stand")),
            Map.entry(Material.PLAYER_WALL_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/armor_stand")),
            Map.entry(Material.SKELETON_SKULL, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/bone")),
            Map.entry(Material.SKELETON_WALL_SKULL, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/bone")),
            Map.entry(Material.WITHER_SKELETON_SKULL, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/coal_block")),
            Map.entry(Material.WITHER_SKELETON_WALL_SKULL, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/coal_block")),
            Map.entry(Material.ZOMBIE_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/rotten_flesh")),
            Map.entry(Material.ZOMBIE_WALL_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/rotten_flesh")),
            Map.entry(Material.CREEPER_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/gunpowder")),
            Map.entry(Material.CREEPER_WALL_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/gunpowder")),
            Map.entry(Material.PIGLIN_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/piglin_banner_pattern")),
            Map.entry(Material.PIGLIN_WALL_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/piglin_banner_pattern")),
            Map.entry(Material.DRAGON_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/dragon_breath")),
            Map.entry(Material.DRAGON_WALL_HEAD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/dragon_breath")),

            // Other entity-rendered blocks without block/<key> atlas sprites
            Map.entry(Material.CHEST, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/oak_planks")),
            Map.entry(Material.TRAPPED_CHEST, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/oak_planks")),
            Map.entry(Material.ENDER_CHEST, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/obsidian")),
            Map.entry(Material.CONDUIT, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/heart_of_the_sea")),
            Map.entry(Material.DECORATED_POT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/terracotta")),
            Map.entry(Material.TRIAL_SPAWNER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/trial_spawner_side_inactive")),
            Map.entry(Material.VAULT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/vault_front_off")),

            // Animated base frames
            Map.entry(Material.CLOCK, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/clock_00")),
            Map.entry(Material.COMPASS, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/compass_00")),
            Map.entry(Material.RECOVERY_COMPASS, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/recovery_compass_00")),

            // Non-standard item names
            Map.entry(Material.CROSSBOW, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/crossbow_standby")),
            Map.entry(Material.FISHING_ROD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/fishing_rod")),
            Map.entry(Material.SHIELD, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/shield")),
            Map.entry(Material.TRIDENT, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/trident")),
            Map.entry(Material.SPYGLASS, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/spyglass")),
            Map.entry(Material.TIPPED_ARROW, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/tipped_arrow_head")),

            // Crop block variants mapped to their item sprites
            Map.entry(Material.CARROTS, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/carrot")),
            Map.entry(Material.POTATOES, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/potato")),
            Map.entry(Material.BEETROOTS, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/beetroot")),

            // Composite blocks with dedicated side/base textures
            Map.entry(Material.SMOOTH_STONE_SLAB, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/smooth_stone_slab_side")),
            Map.entry(Material.BAMBOO_MOSAIC_SLAB, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/bamboo_mosaic")),
            Map.entry(Material.BAMBOO_MOSAIC_STAIRS, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/bamboo_mosaic")),
            Map.entry(Material.PETRIFIED_OAK_SLAB, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/oak_planks")),
            Map.entry(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/gold_block")),
            Map.entry(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/iron_block")),
            Map.entry(Material.MOSS_CARPET, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/moss_block")),
            Map.entry(Material.PALE_MOSS_CARPET, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/pale_moss_block")),
            Map.entry(Material.GLASS_PANE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/glass")),

            // Multi-face / directional blocks without a plain block/<key> texture
            Map.entry(Material.GRASS_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/grass_block_side")),
            Map.entry(Material.PODZOL, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/podzol_side")),
            Map.entry(Material.MYCELIUM, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/mycelium_side")),
            Map.entry(Material.CRIMSON_NYLIUM, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/crimson_nylium_side")),
            Map.entry(Material.WARPED_NYLIUM, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/warped_nylium_side")),
            Map.entry(Material.DIRT_PATH, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/dirt_path_side")),
            Map.entry(Material.SNOW_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/snow")),
            Map.entry(Material.MAGMA_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/magma")),
            Map.entry(Material.SMOOTH_QUARTZ, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/quartz_block_bottom")),
            Map.entry(Material.SMOOTH_SANDSTONE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/sandstone_top")),
            Map.entry(Material.SMOOTH_RED_SANDSTONE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/red_sandstone_top")),
            Map.entry(Material.QUARTZ_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/quartz_block_side")),
            Map.entry(Material.DRIED_KELP_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/dried_kelp_side")),
            Map.entry(Material.HAY_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/hay_block_side")),
            Map.entry(Material.BONE_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/bone_block_side")),
            Map.entry(Material.BASALT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/basalt_side")),
            Map.entry(Material.POLISHED_BASALT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/polished_basalt_side")),
            Map.entry(Material.TNT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/tnt_side")),
            Map.entry(Material.CRAFTING_TABLE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/crafting_table_front")),
            Map.entry(Material.FURNACE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/furnace_front")),
            Map.entry(Material.BLAST_FURNACE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/blast_furnace_front")),
            Map.entry(Material.SMOKER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/smoker_front")),
            Map.entry(Material.DISPENSER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/dispenser_front")),
            Map.entry(Material.DROPPER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/dropper_front")),
            Map.entry(Material.OBSERVER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/observer_front")),
            Map.entry(Material.PISTON, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/piston_side")),
            Map.entry(Material.STICKY_PISTON, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/piston_top_sticky")),
            Map.entry(Material.LOOM, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/loom_front")),
            Map.entry(Material.CARTOGRAPHY_TABLE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/cartography_table_side1")),
            Map.entry(Material.FLETCHING_TABLE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/fletching_table_front")),
            Map.entry(Material.SMITHING_TABLE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/smithing_table_front")),
            Map.entry(Material.STONECUTTER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/stonecutter_side")),
            Map.entry(Material.GRINDSTONE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/grindstone_side")),
            Map.entry(Material.LECTERN, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/lectern_front")),
            Map.entry(Material.COMPOSTER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/composter_side")),
            Map.entry(Material.BARREL, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/barrel_side")),
            Map.entry(Material.BEE_NEST, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/bee_nest_front")),
            Map.entry(Material.BEEHIVE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/beehive_front")),
            Map.entry(Material.PUMPKIN, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/pumpkin_side")),
            Map.entry(Material.MELON, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/melon_side")),
            Map.entry(Material.CACTUS, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/cactus_side")),
            Map.entry(Material.JUKEBOX, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/jukebox_side")),
            Map.entry(Material.LODESTONE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/lodestone_side")),
            Map.entry(Material.RESPAWN_ANCHOR, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/respawn_anchor_side0")),
            Map.entry(Material.ANCIENT_DEBRIS, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/ancient_debris_side")),
            Map.entry(Material.REINFORCED_DEEPSLATE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/reinforced_deepslate_side")),
            Map.entry(Material.CHISELED_BOOKSHELF, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/chiseled_bookshelf_empty")),
            Map.entry(Material.DAYLIGHT_DETECTOR, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/daylight_detector_top")),
            Map.entry(Material.ENCHANTING_TABLE, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/enchanting_table_side")),
            Map.entry(Material.END_PORTAL_FRAME, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/end_portal_frame_side")),
            Map.entry(Material.JIGSAW, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/jigsaw_top")),
            Map.entry(Material.COMMAND_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/command_block_front")),
            Map.entry(Material.CHAIN_COMMAND_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/chain_command_block_front")),
            Map.entry(Material.REPEATING_COMMAND_BLOCK, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/repeating_command_block_front")),
            Map.entry(Material.SUSPICIOUS_SAND, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/suspicious_sand_0")),
            Map.entry(Material.SUSPICIOUS_GRAVEL, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/suspicious_gravel_0")),
            Map.entry(Material.SCULK_SENSOR, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/sculk_sensor_side")),
            Map.entry(Material.CALIBRATED_SCULK_SENSOR, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/calibrated_sculk_sensor_side")),
            Map.entry(Material.SCULK_SHRIEKER, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/sculk_shrieker_side")),
            Map.entry(Material.SCULK_CATALYST, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/sculk_catalyst_side")),
            Map.entry(Material.OCHRE_FROGLIGHT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/ochre_froglight_side")),
            Map.entry(Material.VERDANT_FROGLIGHT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/verdant_froglight_side")),
            Map.entry(Material.PEARLESCENT_FROGLIGHT, new SpriteDefinition(ATLAS_BLOCKS, "minecraft:block/pearlescent_froglight_side"))
    );

    /**
     * Blocks that have a dedicated inventory/item sprite defined by Mojang.
     * These SHOULD NOT fall back to block/<key>.
     */
    private static final Set<Material> INVENTORY_BLOCKS = EnumSet.of(
            // Technical & utility blocks
            Material.BARRIER,
            Material.STRUCTURE_VOID,
            Material.LIGHT,
            Material.REPEATER,
            Material.COMPARATOR,
            Material.LEVER,
            Material.TRIPWIRE_HOOK,
            Material.REDSTONE,
            Material.STRING,
            Material.FLOWER_POT,
            Material.HOPPER,
            Material.CAULDRON,
            Material.BREWING_STAND,
            Material.BELL,
            Material.CAKE,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.SEA_PICKLE,
            Material.ITEM_FRAME,
            Material.GLOW_ITEM_FRAME,
            Material.PAINTING,

            // Plants, flora & placeable crops/seeds
            Material.LILY_PAD,
            Material.VINE,
            Material.GLOW_LICHEN,
            Material.SCULK_VEIN,
            Material.HANGING_ROOTS,
            Material.POINTED_DRIPSTONE,
            Material.SPORE_BLOSSOM,
            Material.BIG_DRIPLEAF,
            Material.SMALL_DRIPLEAF,
            Material.PINK_PETALS,
            Material.WILDFLOWERS,
            Material.LEAF_LITTER,
            Material.SUGAR_CANE,
            Material.BAMBOO,
            Material.KELP,
            Material.SEAGRASS,
            Material.FROGSPAWN,
            Material.SNIFFER_EGG,
            Material.TURTLE_EGG,
            Material.NETHER_WART,
            Material.SWEET_BERRIES,
            Material.GLOW_BERRIES,
            Material.COCOA_BEANS,
            Material.PITCHER_PLANT,
            Material.PITCHER_POD,
            Material.TORCHFLOWER_SEEDS,
            Material.WHEAT,
            Material.WHEAT_SEEDS,
            Material.MELON_SEEDS,
            Material.PUMPKIN_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.CARROT,
            Material.POTATO
    );

    /**
     * Resolves the Atlas and Sprite Key from a raw sprite path string.
     * Supports namespaced keys (e.g., {@code "minecraft:block/stone"}) and shorthand paths
     * (e.g., {@code "block/stone"}, {@code "gui/icon/checkmark"}, {@code "diamond"}).
     */
    public static @NotNull SpriteDefinition resolveSprite(@NotNull String spriteKey) {
        Key key = Key.key(spriteKey);
        String path = key.value();

        Key atlas;
        if (path.startsWith("block/")) {
            atlas = ATLAS_BLOCKS;
        } else if (path.startsWith("gui/")) {
            atlas = ATLAS_GUI;
        } else if (path.startsWith("mob_effect/")) {
            atlas = ATLAS_MOB_EFFECTS;
        } else if (path.startsWith("painting/")) {
            atlas = ATLAS_PAINTINGS;
        } else if (path.startsWith("particle/")) {
            atlas = ATLAS_PARTICLES;
        } else if (path.startsWith("map/decorations/")) {
            atlas = ATLAS_MAP_DECORATIONS;
        } else {
            atlas = ATLAS_ITEMS;
        }

        return new SpriteDefinition(atlas, key);
    }

    /**
     * Resolves the Atlas and Sprite Key for a given Material.
     */
    public static @NotNull SpriteDefinition resolveSprite(@NotNull Material material) {
        // Overrides
        SpriteDefinition override = SPRITE_OVERRIDES.get(material);
        if (override != null) {
            return override;
        }

        // Normalize waxed copper and infested stone variants to their base texture key
        String key = material.key().value();
        if (key.startsWith("waxed_")) {
            key = key.substring("waxed_".length());
        } else if (key.startsWith("infested_")) {
            key = key.substring("infested_".length());
        }

        // For example, oak_door the block face vs oak_door the item sprite that a player would see in their inventory.
        if (INVENTORY_BLOCKS.contains(material) || isItemAtlasBlockKey(key)) {
            String normalizedItemKey = key.endsWith("_sign")
                    ? key.replace("_wall_", "_")
                    : key;
            return new SpriteDefinition(
                    ATLAS_ITEMS,
                    "minecraft:item/" + normalizedItemKey
            );
        }

        // Blocks (including composite shapes like stairs, slabs, walls, fences, buttons, etc.)
        if (material.isBlock()) {
            return new SpriteDefinition(
                    ATLAS_BLOCKS,
                    "minecraft:block/" + resolveBlockTextureKey(key)
            );
        }

        // Default items
        return new SpriteDefinition(
                ATLAS_ITEMS,
                "minecraft:item/" + key
        );
    }

    private static boolean isItemAtlasBlockKey(String key) {
        return key.endsWith("_door")
                || key.endsWith("_sign")
                || key.equals("candle") || key.endsWith("_candle")
                || key.equals("lantern") || key.endsWith("_lantern")
                || key.equals("chain") || key.endsWith("_chain");
    }

    private static String resolveBlockTextureKey(String key) {
        if (key.equals("shulker_box")) {
            return "purpur_block";
        }
        if (key.endsWith("_shulker_box")) {
            return key.substring(0, key.length() - "_shulker_box".length()) + "_terracotta";
        }
        if (key.endsWith("_bed")) {
            return key.substring(0, key.length() - "_bed".length()) + "_wool";
        }
        if (key.endsWith("_wall_banner")) {
            return key.substring(0, key.length() - "_wall_banner".length()) + "_wool";
        }
        if (key.endsWith("_banner")) {
            return key.substring(0, key.length() - "_banner".length()) + "_wool";
        }
        if (key.endsWith("_carpet")) {
            return key.substring(0, key.length() - "_carpet".length()) + "_wool";
        }
        if (key.endsWith("_stained_glass_pane")) {
            return key.substring(0, key.length() - "_pane".length());
        }
        if (key.endsWith("_wood")) {
            return key.substring(0, key.length() - "_wood".length()) + "_log";
        }
        if (key.endsWith("_hyphae")) {
            return key.substring(0, key.length() - "_hyphae".length()) + "_stem";
        }
        if (key.endsWith("_wall_torch")) {
            return key.replace("_wall_torch", "_torch");
        }
        if (key.equals("wall_torch")) {
            return "torch";
        }
        if (key.endsWith("_wall_fan")) {
            return key.replace("_wall_fan", "_fan");
        }

        for (String suffix : List.of("_stairs", "_slab", "_wall", "_fence_gate", "_fence", "_button", "_pressure_plate")) {
            if (key.endsWith(suffix)) {
                String stem = key.substring(0, key.length() - suffix.length());
                if (WOOD_TYPES.contains(stem)) {
                    return stem + "_planks";
                }
                return COMPOUND_BLOCK_BASES.getOrDefault(stem, stem);
            }
        }

        return key;
    }

}