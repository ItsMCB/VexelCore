# 🔷 VexelCore

![License](https://img.shields.io/github/license/itsmcb/vexelcore?color=7c3aed)
![Version](https://img.shields.io/badge/version-1.0.1--SNAPSHOT-7c3aed)
![Paper](https://img.shields.io/badge/Paper-26.3-7c3aed?logo=papermc)
![Java](https://img.shields.io/badge/Java-25-7c3aed?logo=openjdk)
[![Discord](https://img.shields.io/badge/Discord-Libre_Galaxy-7c3aed?logo=discord&logoColor=f5f5f5)](https://discord.gg/86qJJHtDgT)

Developer framework and utility platform for **Paper 26.3** and **Velocity 3.5+** plugins. VexelCore offloads boilerplate with a composable Brigadier command engine, declarative flag parsing, Paper Dialog and Chest Menu UI builders, Java/Bedrock player profile & skin caching, Adventure-native localization, and third-party plugin integrations.

---

## 📑 Table of Contents

- [Requirements & Installation](#-requirements--installation)
- [Developer Setup](#-developer-setup)
- [Vexel Framework](#-vexel-framework)
  - [Context & Service Locator](#context--service-locator)
  - [Composable Brigadier Commands](#composable-brigadier-commands)
  - [Typed Arguments (`VexelArg`)](#typed-arguments-vexelarg)
  - [Declarative Flags (`VexelFlags`)](#declarative-flags-vexelflags)
- [UI Systems](#-ui-systems)
  - [Paper Dialog API Wrappers](#paper-dialog-api-wrappers)
  - [Chest Menu System](#chest-menu-system)
- [Player Profile & Skin Cache](#-player-profile--skin-cache)
- [Configuration & Localization](#-configuration--localization)
- [Utilities & Integrations Reference](#-utilities--integrations-reference)
- [Server Configuration](#-server-configuration)
- [Architecture & Internals](#-architecture--internals)
- [License](#-license)

---

## 🚀 Requirements & Installation

### Requirements
- **Server Platform:** Paper `26.3+` or Velocity `3.5.0+`
- **Java:** `25+`
- **Optional Database (Bukkit):** MariaDB (for persistent cross-restart player profile & skin caching)
- **Optional Server Plugins (Bukkit):**
  - `floodgate`: Bedrock player detection, XUID conversion, and Dialog debounce safeguards
  - `PlaceholderAPI`: Built-in `%vexelcore_*%` expansion and placeholder parsing in `ChatUtils`
  - `FastAsyncWorldEdit` & `WorldGuard`: Schematic operations and region management utilities
  - `CoreProtect`: API retrieval helper

### Server Installation
1. Download `VexelCore-Bukkit-<version>.jar` (for Paper) or `VexelCore-Velocity-<version>.jar` (for Velocity).
2. Place the JAR into your server's `plugins/` directory.
3. Start the server. On Paper, `plugins/VexelCore/config.yml` will be generated automatically.

---

## 🛠️ Developer Setup

### Building Locally
Clone the repository and publish artifacts to your local Maven repository or build the shaded JARs:

```bash
# Build shaded JARs in bukkit/build/libs/ and velocity/build/libs/
./gradlew build

# Publish artifacts (common, bukkit, velocity) to ~/.m2/repository
./gradlew publishToMavenLocal
```

### Gradle Configuration (`build.gradle`)

```groovy
repositories {
    mavenLocal()
    mavenCentral()
    maven { url 'https://repo.papermc.io/repository/maven-public/' }
}

dependencies {
    // Paper plugins
    compileOnly 'me.itsmcb.vexelcore:bukkit:1.0.1-SNAPSHOT'
    compileOnly 'me.itsmcb.vexelcore:common:1.0.1-SNAPSHOT'

    // Or if referencing local build output directly:
    // compileOnly files('../VexelCore/bukkit/build/libs/VexelCore-Bukkit-1.0.1-SNAPSHOT.jar')
}
```

### Declaring the Dependency (`paper-plugin.yml`)

VexelCore includes a `PluginBootstrap` (`VexelCoreBootstrap`) so dependent plugins can safely depend on it during both bootstrap and server runtime phases:

```yaml
dependencies:
  bootstrap:
    VexelCore:
      required: true
      load: BEFORE
      join-classpath: true
  server:
    VexelCore:
      required: true
      load: BEFORE
      join-classpath: true
```

---

## 🧩 Vexel Framework

The `me.itsmcb.vexelcore.bukkit.api.framework` package provides a modern architecture for organizing plugin services and Brigadier commands.

### Context & Service Locator

Extend `AbstractVexelContext` to create a strongly typed context for your plugin. Registered services are automatically indexed by their concrete class as well as all non-JDK interfaces and abstract supertypes.

```java
public class MyPluginContext extends AbstractVexelContext {

    public MyPluginContext(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isFeatureEnabled(@Nullable String featureRoute) {
        if (featureRoute == null) return true;
        return getPlugin().getConfig().getBoolean(featureRoute + ".enabled", true);
    }
}
```

```java
// Registering and retrieving services
MyPluginContext context = new MyPluginContext(this);
context.registerService(new ArenaManager(this));

// Retrieve by class or interface
ArenaManager arenas = context.requireService(ArenaManager.class);
Optional<ArenaManager> optionalArenas = context.findService(ArenaManager.class);
```

---

### Composable Brigadier Commands

Every command node in VexelCore implements `VexelCommandNode`:
- **`VexelRootCommand<C>`**: Root command registered with Paper's `LifecycleEvents.COMMANDS`. Supports subcommands, root-level arguments, or hybrid trees.
- **`VexelCommandGroup<C>`**: Container node that holds child subcommands or nested groups and automatically generates paginated `/cmd help [page]` menus.
- **`VexelPlayerSubCommand<C>`**: Leaf command restricted to `Player` executors (automatically hidden from console in tab-completion).
- **`VexelSenderSubCommand<C>`**: Leaf command executable by any `CommandSender`.

#### Defining a Subcommand

```java
public class TeleportArenaCmd extends VexelPlayerSubCommand<MyPluginContext> {

    private static final VexelArg<String> ARENA_ARG = VexelArg.string("arena", "Target arena name");

    public TeleportArenaCmd(@NotNull MyPluginContext context) {
        super(context, "tp", "Teleport to an arena", "myplugin.arena.tp");
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("teleport");
    }

    @Override
    public @NotNull List<VexelArg<?>> getArguments() {
        return List.of(ARENA_ARG);
    }

    @Override
    public void execute(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull Player player) {
        String arenaName = requireArg(ctx, ARENA_ARG);
        TextUtils.send(player, "<green>Teleporting to <aqua>" + arenaName + "</aqua>...</green>");
    }
}
```

#### Defining & Registering a Root Command

```java
public class ArenaRootCommand extends VexelRootCommand<MyPluginContext> {

    public ArenaRootCommand(@NotNull MyPluginContext context) {
        super(context, "arena", "Manage arenas", "myplugin.arena");
    }

    @Override
    public @NotNull List<VexelCommandNode> getChildren() {
        return List.of(
            new TeleportArenaCmd(context)
        );
    }
}

// In your plugin's onEnable():
VexelCommandRegistry registry = new VexelCommandRegistry(context);
registry.register(new ArenaRootCommand(context));
```

> [!TIP]
> **Cross-Plugin Command Composition:** Because `VexelRootCommand` extends `VexelCommandGroup`, you can attach one plugin's `VexelRootCommand` directly inside another plugin's `getChildren()` list. This feature makes `/parentPlugin childRoot subcommand` work automatically with full tab-completion and help pagination.

---

### Typed Arguments (`VexelArg`)

`VexelArg<T>` wraps Brigadier argument types with hover tooltips, optionality rules, and flexible suggestion providers:

```java
// Built-in convenience factories
VexelArg<String> wordArg       = VexelArg.word("id", "Identifier (no spaces)");
VexelArg<String> stringArg     = VexelArg.string("name", "Quoted string");
VexelArg<String> greedyArg     = VexelArg.greedyString("reason", "Remaining input");
VexelArg<Integer> intArg       = VexelArg.integer("amount", "Item count").asOptional();
VexelArg<String> choiceArg     = VexelArg.choice("mode", "Game mode", List.of("solo", "team"));
VexelArg<String> onlineArg     = VexelArg.onlinePlayer("target", "Online player name");
VexelArg<String> worldArg      = VexelArg.world("world", "Loaded world name");
VexelArg<PlayerSelectorArgumentResolver> selectorArg = VexelArg.player("player", "Player selector");

// Custom builder with dynamic, contextual, or async suggestions and namespaced key matching
VexelArg<String> customKeyArg = VexelArg.<String>builder("dimension", StringArgumentType.string(), String.class, "Dimension key")
    .suggests(() -> List.of("minecraft:overworld", "custom:arena"))
    .suggestionMode(VexelArg.SuggestionMode.NAMESPACED_KEY) // Matches both "custom:arena" and "arena"
    .optional()
    .build();
```

> [!IMPORTANT]
> Optional arguments (`asOptional()` or `.optional()`) must always be placed after required arguments in `getArguments()`. VexelCore validates argument ordering when building the Brigadier tree.

---

### Declarative Flags (`VexelFlags`)

`VexelFlags` provides a state-aware, looping Brigadier suggestion engine and parser for `--flag` and `-f` tokens at the end of a command:

```java
private static final VexelFlags CREATE_FLAGS = VexelFlags.builder()
    .flag(VexelFlag.booleanFlag("no-structures", "Disable structure generation"))
    .flag(VexelFlag.valued("environment", "Dimension environment", List.of("NORMAL", "NETHER", "THE_END")).alias("e"))
    .flag(VexelFlag.multiValued("spawn", "Fixed spawn X Y Z", 3))
    .flag(VexelFlag.multiValued("gamerule", "Set game rule", 2).alias("g").repeatable())
    .build();

private static final VexelArg<String> FLAGS_ARG = CREATE_FLAGS.asArg("flags", "Creation flags");
```

Inside your command's `execute` method:

```java
FlagParser flags = CREATE_FLAGS.parse(optionalArg(ctx, FLAGS_ARG));

boolean noStructures = flags.hasFlag("no-structures");
String env = flags.getValue("environment").orElse("NORMAL"); // Works even if user typed "-e"
List<String> spawnCoords = flags.getValues("spawn");
```

---

## 🖥️ UI Systems

### Paper Dialog API Wrappers

Built on Paper's native Dialog API and managed by `DialogSessionManager` (which automatically handles callback cleanup, container closing, and Floodgate/Bedrock open-delay debouncing).

| Class | Dialog Type | Description |
| :--- | :--- | :--- |
| `VexelNoticeDialog` | Notice | Displays informational messages with a single close/back button. |
| `VexelButtonDialog` | Multi-Action | Displays a multi-column grid of action buttons with optional parent/back routing. |
| `VexelInputDialog` | Confirmation | Collects form inputs (`DialogInput`) and provides typed access via `DialogContext`. |
| `VexelPaginatedDialog<T>` | Multi-Action | Paginates a `List<T>` using sync or async (`CompletableFuture`) button renderers. |

```java
// Multi-action button dialog
new VexelButtonDialog("&5&lServer Selector")
    .addMessage("&7Select a destination server:")
    .setColumns(2)
    .addButton("&aSurvival", "&7Click to connect", p -> VexelCoreBukkitAPI.connectPlayerToServer(p, "survival"))
    .addButton("&bCreative", "&7Click to connect", p -> VexelCoreBukkitAPI.connectPlayerToServer(p, "creative"))
    .open(player);

// Paginated dialog with async entry rendering
new VexelPaginatedDialog<>("Cached Players", playerUUIDs)
    .setItemsPerPage(8)
    .setColumns(2)
    .setAsyncRenderer(uuid -> VexelCoreBukkitAPI.getCacheManager().getCachedPlayer(uuid)
        .thenApply(cp -> VexelPaginatedDialog.ButtonEntry.of(
            ChatUtils.makeLabel(cp, "&a" + cp.getUsername()),
            "&7UUID: &f" + cp.getUUID(),
            clicker -> TextUtils.send(clicker, "<gray>Clicked " + cp.getUsername() + "</gray>")
        )))
    .open(player);
```

---

### Chest Menu System

VexelCore provides `Menu` and `PaginatedMenu` for traditional inventory GUIs, backed by `MenuManager`.

- Buttons are tracked via `PersistentDataContainer` (`vc-menu-system:vc-menu-item-id`) so players cannot steal items or drag items into menu slots.
- Menus automatically refresh visible buttons every 20 ticks while open and clean up their tasks when closed (unless `.persistence(true)` is set).
- `MenuManager#setPreviousMenu(current, previous)` automatically turns the bottom-center exit button on a `PaginatedMenu` into a back button.

```java
PaginatedMenu menu = new PaginatedMenu(MenuRowSize.FOUR, "&8Arena Browser")
    .clickToClose(false);

menu.addButton(new MenuButton(Material.DIAMOND_SWORD)
    .name("&bPvP Arena")
    .addLore("&7Click to join!")
    .leftClick(event -> player.performCommand("arena tp pvp")));

// Supports custom skulls via HeadTexture, CachedPlayerV2, or Base64 texture strings
menu.setToolbarButton(0, new MenuButton(HeadTexture.MONITOR_INFO_MARK)
    .name("&eInfo")
    .addLore("&7Select an arena above."));

VexelCoreBukkitAPI.getMenuManager().open(menu, player);
```

---

## 👤 Player Profile & Skin Cache

`CacheManagerV2` (accessed via `VexelCoreBukkitAPI.getCacheManager()`) resolves player UUIDs, usernames, editions (`JAVA` or `BEDROCK`), and signed skin textures (`PlayerSkinData`) asynchronously without blocking the main server thread.

### Resolution Order
1. **In-Memory Cache** (`ConcurrentHashMap`)
2. **Online Session** (derives directly from `Player#getPlayerProfile()` if online)
3. **MariaDB Database** (if configured in `plugins/VexelCore/config.yml`)
4. **External APIs** (Mojang Session/Profile API for Java; MCProfile, Floodgate cache, and Geyser API for Bedrock, with MineSkin fallback for skin signing)

```java
CacheManagerV2 cache = VexelCoreBukkitAPI.getCacheManager();

// Lookup by username, UUID (with or without hyphens), or online Player
cache.getCachedPlayer("ItsMCB").thenAccept(cachedPlayer -> {
    UUID uuid = cachedPlayer.getUUID();
    CachedPlayerV2.Edition edition = cachedPlayer.getEdition(); // JAVA or BEDROCK
    PlayerSkinData skin = cachedPlayer.getPlayerSkinData();

    // Create a player head ItemStack or chat component
    ItemStack skull = new SkullBuilderUtil(skin).get();
    Component headLabel = ChatUtils.makeLabel(cachedPlayer, "&f" + cachedPlayer.getUsername());
}).exceptionally(ex -> {
    if (PluginUtils.throwableIs(ex, PlayerNotFoundException.class)) {
        // Handle unknown player
    }
    return null;
});
```

---

## ⚙️ Configuration & Localization

### `BoostedConfig`
Located in `common`, `BoostedConfig` wraps **BoostedYAML** with automatic schema updates driven by `file-version`:

```java
BoostedConfig config = new BoostedConfig(
    getDataFolder(),
    "config",
    getResource("config.yml"),
    SpigotSerializer.getInstance()
);

String value = config.get().getString("my-setting");
config.save();
config.reload();
```

### Unloaded-World-Safe Locations (`BukkitLocation` & `BukkitCoordinates`)
Standard Bukkit `Location` deserialization fails if the target world is not yet loaded. VexelCore provides `BukkitCoordinates` and `BukkitLocation` (`ConfigurationSerializable`) plus a migration helper `BukkitLocation.stripLegacyLocationTags(file)` to safely store and load coordinates.

### `LocalizationManager`
Integrates `BoostedConfig` language files (`resources/lang/<locale>.yml`) with Adventure's `GlobalTranslator` and `MessageFormat`:

```java
LocalizationManager i18n = new LocalizationManager(
    this,
    "myplugin",
    List.of(Locale.US, Locale.GERMANY)
);

// Automatically renders in the player's client locale with variable substitution
i18n.sendMsg(player, "welcome.message", Component.text(player.getName()));
```

---

## 🧰 Utilities & Integrations Reference

### Bukkit Module (`me.itsmcb.vexelcore.bukkit.*`)

| Class | Package | Highlights |
| :--- | :--- | :--- |
| `VexelCoreBukkitAPI` | `bukkit` | Static entry point for `CacheManagerV2`, `MenuManager`, `DialogSessionManager`, client brand cache, and BungeeCord/Velocity server transfers. |
| `BukkitFeature` / `BukkitFeatureManager` | `bukkit` / `api.managers` | Groups related `Listener`s, `Command`s, and `VexelCoreRunnable` tasks for runtime enable/disable/reload. |
| `TextUtils` | `api.framework` | MiniMessage helpers: `mm()` (trusted tags), `mmSafe()` (formatting-only for user input), `sendCopyable()`, `sendHiddenCopyable()`, and `sendUrl()`. |
| `TaskProgress` | `api.framework` | Periodic task feedback reporter supporting `CHAT`, `ACTIONBAR`, and `TITLE` strategies. |
| `ChatUtils` | `api.utils` | Legacy/hex colorizing (`&#RRGGBB`), format-preserving word wrap (`splitText`), PAPI resolution, and Adventure object components (`getHeadComponent`, `makeLabel` with `HeadTexture`, `CachedPlayerV2`, or `Material` item/block sprites). |
| `BukkitMsgBuilder` | `api.text` | Fluent builder for legacy/hex messages with hover text, click events, action bar repeaters, and YAML serialization. |
| `DirectionUtils` | `api.utils` | Parses compass directions (`NORTH`, `SOUTHEAST`), relative directions (`left`, `right`, `back`, `forward`), and numeric yaw with tab-completion lists. |
| `LocationUtils` | `api.utils` | `getHighestSafeLocation()`, `isSafeSolidBlock()`, `toCenter()`, and `entityLookAtPlayer()`. |
| `WorldUtils` | `api.utils` | World folder discovery, player evacuation (`kickAllFromWorld`), world unloading, time-of-day names, and moon phase calculation. |
| `WorldEditUtils` | `api.utils` | Async/sync schematic pasting, `.schem` creation, and cuboid region expansion via FastAsyncWorldEdit. |
| `WorldGuardUtilities` | `api.utils` | Region queries at locations, ownership checks, `__global__` region creation, and region resizing. |
| `PluginUtils` | `api.utils` | Safe `CoreProtectAPI` retrieval, plugin state checks, and `throwableIs()` cause-chain inspection. |
| `SkullBuilderUtil` & `PlayerUtils` | `api.utils` | Builds textured `PLAYER_HEAD` items and applies textures/signatures to online `PlayerProfile`s. |

### Common Module (`me.itsmcb.vexelcore.common.*`)

| Class | Package | Highlights |
| :--- | :--- | :--- |
| `CommandNavigation` | `api.command` | Renders clickable Adventure pagination footers (`« Prev \| Page X of Y \| Next »`). |
| `CooldownManager` | `api.manager` | Thread-safe per-UUID cooldown tracker. |
| `HeadTexture` & `Icon` | `api` / `api.text` | Curated Base64 player head textures (arrows, planets, moon phases, weather, terracotta UI icons) and Unicode chat symbols. |
| `FileUtils` & `ZipUtils` | `api.utils` | Recursive directory copy/deletion, human-readable file/disk size formatting, and directory zipping. |
| `StringUtils` & `TimeUtils` | `api.utils` | UUID validation/derivation (with or without hyphens, Floodgate XUID patterns) and epoch/duration formatting. |

### Velocity Module (`me.itsmcb.vexelcore.velocity.*`)

| Class | Package | Highlights |
| :--- | :--- | :--- |
| `VexelCoreVelocity` | `velocity` | Velocity proxy plugin entry point providing access to `common` utilities and `BoostedConfig`. |
| `Msg` | `api.utils` | Legacy `&` colorization and message sending helper for Velocity `CommandSource` and `Player`. |

---

## 🔧 Server Configuration

### `plugins/VexelCore/config.yml` (Bukkit)

```yaml
#
# VexelCore Config
#

# Do not edit this!
file-version: 1

# Player cache details
player-cache:
  host: ""
  port: 3306
  database: "playercache"
  user: "playercache"
  password: ""
  api-keys:
    mcprofile: ""
    mineskin: ""
```

> [!NOTE]
> If `player-cache.host` is left blank or MariaDB is unreachable on startup, `CacheManagerV2` automatically falls back to in-memory caching for the duration of the server session. Providing `mcprofile` and `mineskin` API keys is optional and improves rate limits and skin-signing reliability during fallback lookups.

### PlaceholderAPI Placeholders

When `PlaceholderAPI` is installed on Paper, VexelCore automatically registers the `vexelcore` expansion:
- `%vexelcore_version%`: Returns the loaded VexelCore version.
- `%vexelcore_servers%`: Comma-separated list of proxy server names discovered via the `BungeeCord` plugin messaging channel.
- `%vexelcore_player_uuid%`: The player's UUID.
- `%vexelcore_player_brand_by_uuid_<uuid>%`: Returns the client brand (e.g., `vanilla`, `fabric`) recorded via the `minecraft:brand` channel.

---

## 🏗️ Architecture & Internals

- **Multi-Module Structure:**
  - `common`: Platform-agnostic utilities, `BoostedConfig`, `CommandNavigation`, `HeadTexture`, and `Icon`.
  - `bukkit`: Paper 26.3 plugin and API (`VexelCore-Bukkit`), shading `common` and relocated `boosted-yaml`.
  - `velocity`: Velocity 3.5.0 plugin and API (`VexelCore-Velocity`), shading `common` and relocated `boosted-yaml`.
- **Paper Library Loader (`VexelCoreLoader`):** Non-plugin runtime libraries (such as `org.mariadb.jdbc:mariadb-java-client` and `boosted-yaml`) are resolved via Paper's `MavenLibraryResolver` using Paper's official Maven Central mirror.
- **Dialog Session Garbage Collection:** `DialogSessionManager` scopes generated click callback keys (`vexel:btn/<uuid>`) per player session, invalidates stale keys on inventory close, quit, or new dialog open, and runs an async cleanup task every 5 minutes.

---

## ⚖️ License

VexelCore is licensed under the **[GNU General Public License v3.0](LICENSE)**.