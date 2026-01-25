package me.itsmcb.vexelcore.bukkit.api.utils;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtils {

    public static final Key ATLAS_ITEMS = Key.key("minecraft:items");
    public static final Key ATLAS_BLOCKS = Key.key("minecraft:blocks");

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
     * Pattern to strip all known color and format codes.
     * The trailing '+' makes it match consecutive codes (e.g., "&c&l") as a single unit for replacement.
     */
    private static final Pattern STRIP_CODES_PATTERN =
            Pattern.compile("(?i)(?:[&§](?:#[0-9A-F]{6}|[0-9A-FK-ORL]))+");

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
        return splitText(text,DEFAULT_SPLIT_LENGTH);
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
     * Produces a component showing the head to the left and the text to the right.
     */
    public static Component makeLabel(HeadTexture head, String text) {
        Component icon = ChatUtils.getHeadComponent(head);
        Component label = new BukkitMsgBuilder(" "+text).get();
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

    /**
     * Produces a component showing a sprite to the left and text to the right.
     */
    public static Component makeLabel(String spriteKey, String text) {
        return Component.text()
                .append(Component.object(ObjectContents.sprite(ATLAS_ITEMS, Key.key(spriteKey))))
                .append(Component.space())
                .append(new BukkitMsgBuilder(text).get())
                .build();
    }

    /**
     * Produces a component with a sprite to the left and text to the right.
     * This method attempts to resolve whether the material should use the 'blocks' or 'items' atlas.
     *
     * @param material The material to display.
     * @param text The text to append.
     * @return Component with sprite and text.
     */
    public static Component makeLabel(Material material, String text) {
        SpriteDefinition spriteDef = resolveSprite(material);

        return Component.text()
                .append(Component.object(ObjectContents.sprite(
                        spriteDef.atlas,
                        Key.key(spriteDef.key)
                )))
                .append(Component.space())
                .append(new BukkitMsgBuilder(text).get())
                .build();
    }

    private record SpriteDefinition(Key atlas, String key) {}


    private static final Map<Material, SpriteDefinition> SPRITE_OVERRIDES = Map.ofEntries(
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

            // Blocks whose inventory texture lives ONLY in the item atlas
            Map.entry(Material.BARRIER, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/barrier")),
            Map.entry(Material.STRUCTURE_VOID, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/structure_void")),
            Map.entry(Material.LIGHT, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/light")),
            Map.entry(Material.REPEATER, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/repeater")),
            Map.entry(Material.COMPARATOR, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/comparator")),
            Map.entry(Material.FLOWER_POT, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/flower_pot")),
            Map.entry(Material.HOPPER, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/hopper")),
            Map.entry(Material.CAULDRON, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/cauldron")),
            Map.entry(Material.BREWING_STAND, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/brewing_stand")),
            Map.entry(Material.ITEM_FRAME, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/item_frame")),
            Map.entry(Material.GLOW_ITEM_FRAME, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/glow_item_frame")),
            Map.entry(Material.PAINTING, new SpriteDefinition(ATLAS_ITEMS, "minecraft:item/painting"))
    );


    /**
     * Blocks that have a dedicated inventory/item sprite defined by Mojang.
     * These SHOULD NOT fall back to block/<key>.
     */
    private static final Set<Material> INVENTORY_BLOCKS = EnumSet.of(
            // Plants & flat blocks
            Material.LILY_PAD,
            Material.VINE,
            Material.SUGAR_CANE,
            Material.BAMBOO,
            Material.KELP,
            Material.SEAGRASS,

            // Redstone & utility
            Material.LEVER,
            Material.TRIPWIRE_HOOK,
            Material.REDSTONE,
            Material.STRING,

            // Doors, buttons, signs (inventory variants exist)
            Material.OAK_DOOR,
            Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR,
            Material.MANGROVE_DOOR,
            Material.CHERRY_DOOR,
            Material.BAMBOO_DOOR,

            Material.STONE_BUTTON,
            Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON,
            Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON,
            Material.MANGROVE_BUTTON,
            Material.CHERRY_BUTTON,
            Material.BAMBOO_BUTTON
    );

    /**
     * Resolves the Atlas and Sprite Key for a given Material.
     */
    private static SpriteDefinition resolveSprite(Material material) {
        final String key = material.key().value();

        // Overrides
        SpriteDefinition override = SPRITE_OVERRIDES.get(material);
        if (override != null) {
            return override;
        }

        // For example, oak_door the block face vs oak_door the item spite that a player would see in their inventory.
        if (INVENTORY_BLOCKS.contains(material)) {
            return new SpriteDefinition(
                    ATLAS_ITEMS,
                    "minecraft:item/" + key
            );
        }

        // Blocks
        if (material.isBlock()) {

            // Special-case blocks whose primary visible face is well-defined
            // and commonly expected in UI contexts.
            if (material == Material.GRASS_BLOCK) {
                return new SpriteDefinition(
                        ATLAS_BLOCKS,
                        "minecraft:block/grass_block_side"
                );
            }

            return new SpriteDefinition(
                    ATLAS_BLOCKS,
                    "minecraft:block/" + key
            );
        }

        // Default items
        return new SpriteDefinition(
                ATLAS_ITEMS,
                "minecraft:item/" + key
        );
    }

}