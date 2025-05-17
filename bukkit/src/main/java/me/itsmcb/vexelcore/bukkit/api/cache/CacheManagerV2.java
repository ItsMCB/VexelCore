package me.itsmcb.vexelcore.bukkit.api.cache;

import me.itsmcb.vexelcore.bukkit.VexelCoreBukkit;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.DataRequestFailure;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.DataSaveFailure;
import me.itsmcb.vexelcore.bukkit.api.cache.exceptions.PlayerNotFoundException;
import me.itsmcb.vexelcore.common.api.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CacheManager responsible for managing the cache of player data.
 */
public class CacheManagerV2 {

    public static final String DEFAULT_STEVE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTc0MTA0ODE4NzE4MywKICAicHJvZmlsZUlkIiA6ICJhNjg3N2RkYmE3Zjk0ZWM1ODk2N2NkYjE5MTdlNTNhZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTY2FsaWVyU2NhcmFiODQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFmNDc3ZWIxYTdiZWVlNjMxYzJjYTY0ZDA2ZjhmNjhmYTkzYTMzODZkMDQ0NTJhYjI3ZjQzYWNkZjFiNjBjYiIKICAgIH0KICB9Cn0=";
    public static final String DEFAULT_STEVE_SIGNATURE = "BkGxZAbQGLWlfYjVs5hwIPJmZynqw2NPk5rqvbiW89dE7dnBnbFvkYG9tNkE4v2Ie/CBSMd7p32L9baQyfPkHGsbR09eCAr19fWPnVfAzjKehTa8ipUSGMyNhHjV6V6ZNHMYxjf60X+Ktyro8ua5sNwVcFQV6QSIpvPqRZB2UkybKfzk4Alw2ZcGt4j5dxxxnS/BQYafFekXjLr9nZ0Qdiafu3MZRWu1pmbM+LMldkQWHOsiM5qA22yM/BxW2d2Q+SXDCILQwY4QJo3fs0bW89xKDFufgUyArE1qVgd72ouAUzcQvgH7Lzy38D0wVmXvi0C4H95w+y6o6jOwuWy8HgUrqlQXUY69yF+8ZTnkiBI4ndKEIs8rFLDpvpXMBbK1XBpRegbxBDYu+8HSDoOfbL8Z1stHdXtRcKFbkkhNEzT/mOwmGr/prNn3iVoc8nJ82H9PEx7cTeSkDnEyI0aTVcHzgNMsNzMFmOY8jTg5a5mkyqv0zqvtE6Ws/nCRLEtA+8KFMyqBdAVbvNoHcsIK7FXPb7gFaM4Ako/t02XoVbfoIOaZTL5Xgkl7nBfu/Wgf9bTtat9vUYhVpf/8+ybAVMAHKBZfYaBjt3TKx66/eGqm1Yn4fhQ6ZSVBmcLQLv+1hex3EWu0yS/wj2hv5nNicEvk4DAeVMJKgE3JnZkkI9c=";

    private final Logger log;

    private final ConcurrentHashMap<String, CachedPlayerV2> usernameCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CachedPlayerV2> uuidCache = new ConcurrentHashMap<>();
    private final List<String> invalidUsernameAndUUID = new CopyOnWriteArrayList<>();
    private final MinecraftAPIUtils apiUtils;
    private MariaDbDataSource dataSource;
    private boolean localCachingOnly = false;

    /**
     * Constructs a new CacheManager with the given database connection parameters.
     *
     * @param host     Database host
     * @param port     Database port
     * @param database Database name
     * @param username Database username
     * @param password Database password
     */
    public CacheManagerV2(@NotNull VexelCoreBukkit instance, @NotNull String host, int port, @NotNull String database, @NotNull String username, @NotNull String password, @NotNull String mcProfileAPIKey, @NotNull String mineSkinAPIKey) {
        this.log = instance.getLogger();
        try {
            dataSource = new MariaDbDataSource();
            dataSource.setUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
            dataSource.setUser(username);
            dataSource.setPassword(password);
            // Establish database connection. Only local caching will be available upon failure.
            initialize();
        } catch (SQLException e) {
            log.warning("VexelCore will only use in-memory caching for player data. It's highly suggested to allocate a MariaDB database for extended caching to help prevent API request issues. If this behavior was not expected (i.e., you are trying to set up a database), the following is the exact database error: "+e.getMessage());
            // Local caching variable establishes that a connection was not made. This helps prevent database no connection errors as this is checked before trying to connect
            localCachingOnly = true;
        }
        this.apiUtils = new MinecraftAPIUtils(mcProfileAPIKey, mineSkinAPIKey);
    }

    /**
     * Initializes the database tables if they don't exist.
     *
     * @throws SQLException If a database error occurs
     */
    private void initialize() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Create players table
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS players (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " + // It's not practically worth it to store UUIDs as BINARY(16)
                            "username VARCHAR(16) NOT NULL, " +
                            "edition ENUM('JAVA', 'BEDROCK') NOT NULL, " +
                            "texture TEXT, " +
                            "signature TEXT, " +
                            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                            "UNIQUE INDEX username_idx (username)" + // Index speeds up username queries
                            ")"
            );
        }
        log.info("CacheManager has been successfully initialized with a database!");
    }

    public CompletableFuture<CachedPlayerV2> getCachedPlayer(@NotNull Player player) {
        return getCachedPlayer(player.getUniqueId());
    }

    /**
     * Gets a cached player by username or valid UUID (with or without hyphens). If not in cache, fetches from database or API.
     *
     * @param username The player's username
     * @return CompletableFuture containing the CachedPlayer or null if not found
     */
    public CompletableFuture<CachedPlayerV2> getCachedPlayer(@NotNull String username) {
        // Redirect to UUID method if a UUID was provided
        if (StringUtils.isUUID(username)) {
            return getCachedPlayer(StringUtils.deriveUUID(username));
        }
        // Validate input
        if (!isValidUsernameFormat(username)) {
            return CompletableFuture.failedFuture(new PlayerNotFoundException(username));
        }
        // Check in-memory cache first
        CachedPlayerV2 cachedPlayer = usernameCache.get(username);
        if (cachedPlayer != null) {
            return CompletableFuture.completedFuture(cachedPlayer);
        }
        // Return early if it's an invalid username
        if (invalidUsernameAndUUID.contains(username)) {
            return CompletableFuture.failedFuture(new PlayerNotFoundException(username));
        }

        // Obtain session values if online (must be done synchronously due to Bukkit)
        Optional<Player> op = Bukkit.getOnlinePlayers().stream()
                .map(player -> (Player)player)
                .filter(p -> p.getName().equalsIgnoreCase(username))
                .findFirst();
        if (op.isPresent()) {
            CachedPlayerV2 cp = deriveCachedPlayerFromInGameSession(op.get());
            try {
                saveOrUpdatePlayer(cp);
                // Add to in-game memory cache with an ensured skin
                usernameCache.put(cp.getUsername(), cp);
                uuidCache.put(cp.getUUID(), cp);
            } catch (DataSaveFailure | DataRequestFailure e) {
                throw new RuntimeException(e); // Exception will be captured by the CompletableFuture
            }
            return CompletableFuture.completedFuture(cp);
        }

        return CompletableFuture.supplyAsync(() -> {
            // Check database if not just locally caching
            if (!localCachingOnly) {
                try {
                    CachedPlayerV2 player = getPlayerFromDatabase(username);
                    // Add to in-memory cache
                    usernameCache.put(username, player);
                    uuidCache.put(player.getUUID(), player);
                    return player;
                } catch (DataRequestFailure e) {
                    log.warning("Username-based database fetch for "+username+" encountered a data request failure. Attempting to recover via external API request. Error: "+e.getMessage());
                } catch (PlayerNotFoundException ignored) {
                    // Not cached in the database yet (no problem)
                }
            }

            // Determine if player is Java or Bedrock based on username, then fetch from the appropriate API
            try {
                if (GeyserUtils.isBedrock(username)) {
                    CachedPlayerV2 cachedPlayerV2 = apiUtils.fetchBedrockPlayer(username);
                    saveOrUpdatePlayer(cachedPlayerV2);
                    return cachedPlayerV2;
                }
                // Must be Java
                CachedPlayerV2 cachedPlayerV2 = apiUtils.fetchJavaPlayer(username);
                saveOrUpdatePlayer(cachedPlayerV2);
                return cachedPlayerV2;
            } catch (DataSaveFailure | DataRequestFailure | PlayerNotFoundException e) {
                invalidUsernameAndUUID.add(username);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets a cached player by UUID. If not in cache, fetches from database or API.
     *
     * @param uuid The player's UUID
     * @return CompletableFuture containing the CachedPlayer or null if not found
     */
    public CompletableFuture<CachedPlayerV2> getCachedPlayer(@NotNull UUID uuid) {
        // Check in-memory cache first
        CachedPlayerV2 cachedPlayer = uuidCache.get(uuid);
        if (cachedPlayer != null) {
            return CompletableFuture.completedFuture(cachedPlayer);
        }
        // Return early if it's an invalid username
        if (invalidUsernameAndUUID.contains(uuid.toString())) {
            return CompletableFuture.failedFuture(new PlayerNotFoundException(uuid));
        }

        // Obtain session values if online (must be done synchronously due to Bukkit)
        Optional<Player> op = Bukkit.getOnlinePlayers().stream()
                .map(player -> (Player)player)
                .filter(p -> p.getUniqueId().equals(uuid))
                .findFirst();
        if (op.isPresent()) {
            CachedPlayerV2 cp = deriveCachedPlayerFromInGameSession(op.get());
            try {
                saveOrUpdatePlayer(cp);
                // Add to in-game memory cache with an ensured skin
                usernameCache.put(cp.getUsername(), cp);
                uuidCache.put(cp.getUUID(), cp);
            } catch (DataSaveFailure | DataRequestFailure e) {
                throw new RuntimeException(e); // Exception will be captured by the CompletableFuture
            }
            return CompletableFuture.completedFuture(cp);
        }

        return CompletableFuture.supplyAsync(() -> {
            // Check database if not just locally caching
            if (!localCachingOnly) {
                try {
                    CachedPlayerV2 player = getPlayerFromDatabase(uuid);
                    // Add to in-memory cache
                    usernameCache.put(player.getUsername(), player);
                    uuidCache.put(uuid, player);
                    return player;
                } catch (DataRequestFailure e) {
                    log.warning("UUID-based database fetch for "+uuid+" encountered a data request failure. Attempting to recover via external API request. Error: "+e.getMessage());
                } catch (PlayerNotFoundException ignored) {
                    // Not cached in the database yet (no problem)
                }
            }

            // Determine if player is Java or Bedrock based on username, then fetch from the appropriate API
            try {
                if (GeyserUtils.isBedrock(uuid)) {
                    CachedPlayerV2 cachedPlayerV2 = apiUtils.fetchBedrockPlayer(uuid);
                    saveOrUpdatePlayer(cachedPlayerV2);
                    return cachedPlayerV2;
                }
                // Must be Java
                CachedPlayerV2 cachedPlayerV2 = apiUtils.fetchJavaPlayer(uuid);
                saveOrUpdatePlayer(cachedPlayerV2);
                return cachedPlayerV2;
            } catch (DataSaveFailure | DataRequestFailure | PlayerNotFoundException e) {
                invalidUsernameAndUUID.add(uuid.toString());
                throw new RuntimeException(e);
            }
        });
    }


    public CompletableFuture<List<CachedPlayerV2>> getCachedPlayers(List<UUID> uuids) {
        // Create player futures for each uuid
        List<CompletableFuture<CachedPlayerV2>> playerFutures = uuids.stream()
                .map(this::getCachedPlayer)
                .toList();
        // Wait for each future to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(playerFutures.toArray(new CompletableFuture[0]));
        // Collect finished results
        return allOf.thenApply(ignored -> playerFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    /**
     * Creates a CachedPlayer object from an online player
     *
     * @param player The online player to create a CachedPlayer from.
     * @return CachedPlayer
     */
    private CachedPlayerV2 deriveCachedPlayerFromInGameSession(@NotNull Player player) {
        AtomicReference<String> atomicTexture = new AtomicReference<>(null);
        AtomicReference<String> atomicSignature = new AtomicReference<>(null);
        player.getPlayerProfile().getProperties().forEach(profileProperty -> {
            if (profileProperty.getName().equals("textures")) {
                atomicTexture.set(profileProperty.getValue());
                atomicSignature.set(profileProperty.getSignature());
            }
        });
        return new CachedPlayerV2(
                player.getUniqueId(),
                player.getName(),
                GeyserUtils.isBedrock(player.getUniqueId()) ? CachedPlayerV2.Edition.BEDROCK : CachedPlayerV2.Edition.JAVA,
                atomicTexture.get(),
                atomicSignature.get()
        );
    }

    /**
     * Saves or updates player data in the database based on their current online session (PlayerProfile) data.
     *
     * @param player The online player to save or update information on
     * @throws DataSaveFailure   If there is an error during database save operation.
     * @throws DataRequestFailure If the provided player data is incomplete.
     */
    public void update(Player player) throws DataSaveFailure, DataRequestFailure {
        saveOrUpdatePlayer(deriveCachedPlayerFromInGameSession(player));
    }

    /**
     * Saves or updates player data in the database.
     *
     * @param player The cached player data to save or update.
     * @throws DataSaveFailure   If there is an error during database save operation.
     * @throws DataRequestFailure If the provided player data is incomplete.
     */
    private void saveOrUpdatePlayer(CachedPlayerV2 player) throws DataSaveFailure, DataRequestFailure {
        // Missing skin information is permissible, but missing other elements is unacceptable and calls for an error.
        if (player.getUsername() == null || player.getUUID() == null || player.getEdition() == null) {
            throw new DataRequestFailure("Upon reaching the database save function, the cached profile is incomplete:  "+player.toStringMinimized());
        }

        // Cache in-memory
        usernameCache.put(player.getUsername(), player);
        uuidCache.put(player.getUUID(), player);

        // If a connection to the database was not successful during initialization, forgo database operations.
        if (dataSource == null || localCachingOnly) {
            // Still deciding if providing this information is useful or not
            log.info("Added "+player.getUsername()+" ("+player.getUUID()+") to in-memory cache");
            return;
        }

        // Do not save incomplete data
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            String formattedUUID = GeyserUtils.formatUUID(String.valueOf(player.getUUID()));

            // Use ON DUPLICATE KEY UPDATE for players table
            PreparedStatement upsertPlayer = connection.prepareStatement(
                    "INSERT INTO players (uuid, username, edition, texture, signature) " +
                            "VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "username = VALUES(username), " +
                            "edition = VALUES(edition), " +
                            "texture = VALUES(texture), " +
                            "signature = VALUES(signature)"
            );
            upsertPlayer.setString(1, formattedUUID);
            upsertPlayer.setString(2, player.getUsername());
            upsertPlayer.setString(3, player.getEdition().name());
            upsertPlayer.setString(4,
                    player.getPlayerSkinData().getTexture().equals(DEFAULT_STEVE_TEXTURE) ? null : player.getPlayerSkinData().getTexture()
            ); // Don't save default skin
            upsertPlayer.setString(5,
                    player.getPlayerSkinData().getSignature().equals(DEFAULT_STEVE_SIGNATURE) ? null : player.getPlayerSkinData().getSignature()
            ); // Don't save default skin
            upsertPlayer.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            throw new DataSaveFailure(e);
        }
    }

    /**
     * Gets a player from the database by username.
     *
     * @param username The player's username
     * @return CachedPlayer
     * @throws PlayerNotFoundException If the player was not found
     * @throws DataRequestFailure If a database error occurred
     */
    private CachedPlayerV2 getPlayerFromDatabase(@NotNull String username) throws PlayerNotFoundException, DataRequestFailure {
        return getPlayerFromDatabase("username", username);
    }

    /**
     * Gets a player from the database by UUID.
     *
     * @param uuid The player's UUID
     * @return CachedPlayer
     * @throws PlayerNotFoundException If the player was not found
     * @throws DataRequestFailure If a database error occurred
     */
    private CachedPlayerV2 getPlayerFromDatabase(@NotNull UUID uuid) throws PlayerNotFoundException, DataRequestFailure {
        return getPlayerFromDatabase("uuid", GeyserUtils.formatUUID(uuid.toString()));
    }

    /**
     * Gets a player from the database by a specified field and value.
     *
     * @param field The database field to search by ("username" or "uuid")
     * @param value The value to search for
     * @return CachedPlayer
     * @throws PlayerNotFoundException If the player was not found
     * @throws DataRequestFailure If a database error occurred
     */
    private CachedPlayerV2 getPlayerFromDatabase(@NotNull String field, @NotNull String value) throws PlayerNotFoundException, DataRequestFailure {
        try {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT p.username, p.uuid, p.edition, p.texture, p.signature " +
                                "FROM players p " +
                                "WHERE p." + field + " = ?"
                );
                statement.setString(1, value);

                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    CachedPlayerV2.Edition edition = CachedPlayerV2.Edition.valueOf(result.getString("edition"));
                    String texture = result.getString("texture");
                    String signature = result.getString("signature");

                    return new CachedPlayerV2(
                            UUID.fromString(result.getString("uuid")),
                            result.getString("username"),
                            edition,
                            texture,
                            signature
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataRequestFailure(e);
        }
        throw new PlayerNotFoundException(value);
    }

    /**
     * Gets all players from the database.
     * All players will be added to the in-memory cache.
     *
     * @return List of CachedPlayers from memory or database
     * @throws DataRequestFailure If a database error occurs
     */
    public List<CachedPlayerV2> getAllPlayers() throws DataRequestFailure {
        List<CachedPlayerV2> players = new ArrayList<>();

        // Return in-memory cache if only local caching
        if (localCachingOnly) {
            return usernameCache.values().stream().toList();
        }

        // Retrieve data from database
        try {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT p.username, p.uuid, p.edition, p.texture, p.signature " +
                                "FROM players p "
                );

                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    CachedPlayerV2.Edition edition = CachedPlayerV2.Edition.valueOf(result.getString("edition"));
                    String texture = result.getString("texture");
                    String signature = result.getString("signature");

                    CachedPlayerV2 player = new CachedPlayerV2(
                            UUID.fromString(result.getString("uuid")),
                            result.getString("username"),
                            edition,
                            texture,
                            signature
                    );
                    players.add(player);
                    // Update in-memory cache
                    usernameCache.put(player.getUsername(), player);
                    uuidCache.put(player.getUUID(), player);
                }
            }
        } catch (SQLException e) {
            throw new DataRequestFailure(e);
        }
        return players;
    }

    /**
     * Obtain all known usernames from local cache
     */
    public List<String> getAllPlayerUsernames() {
        return new ArrayList<>(usernameCache.keySet());
    }

    /**
     * Clears the in-memory cache.
     */
    public void clearCache() {
        usernameCache.clear();
        uuidCache.clear();
    }

    /**
     * Checks if a given input string is a valid Minecraft username.
     * <p>
     * A valid username must meet the following criteria:
     * <ul>
     * <li>It must be at least 3 characters long</li>
     * <li>Its length is 16 characters PLUS the length of the Geyser player prefix (if applicable)</li>
     * </ul>
     * <p>
     * Note: The 16-character limit is based on Minecraft: Java Edition and Xbox Live username restrictions.
     * The length of the Geyser player prefix, obtained through {@link FloodgateApi#getPlayerPrefix()}'s length,
     * is added to the maximum allowed username length.
     *
     * @param input The string to validate as a username.
     * @return {@code true} if the input is a valid username, {@code false} otherwise.
     */
    public static boolean isValidUsernameFormat(String input) {
        // Note that 16 characters is the limit for Minecraft: Java Edition and Xbox Live usernames.
        int maxUsernameLength = 16;
        int geyserPrefixLength = 0;
        if (GeyserUtils.isBedrock(input)) {
            geyserPrefixLength = FloodgateApi.getInstance().getPlayerPrefix().length();
        }
        return !input.trim().isEmpty() && input.length() > 2 && (input.length() <= (maxUsernameLength+geyserPrefixLength));
    }

    /**
     * Utility to format the names of multiple players.
     * @return Comma separated list of player usernames or "None"
     */
    public static String formatPlayerNames(@NotNull List<CachedPlayerV2> players) {
        if (players.isEmpty()) {
            return "None";
        }
        return String.join(", ",players.stream().map(CachedPlayerV2::getUsername).toList());
    }
}