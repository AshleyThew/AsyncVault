package com.asyncvault.api.chat;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;

import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for chat formatting service implementations.
 *
 * <p>Provides prefix/suffix and generic info node support for player chat formatting.
 */
public abstract class ChatProvider {

    private final ExecutionProvider executionProvider;

    protected ChatProvider() {
        this(ExecutionProviderContext.require());
    }

    protected ChatProvider(ExecutionProvider executionProvider) {
        this.executionProvider = Objects.requireNonNull(executionProvider, "executionProvider");
    }

    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }

    /**
     * @return The name of the chat backend (e.g., "LuckPerms", "PermissionsEx")
     */
    public abstract String getName();

    /**
     * @return true if this provider supports asynchronous operations natively
     */
    public abstract boolean supportsAsyncOperations();

    /**
     * @return true if this provider supports world-scoped prefixes/suffixes
     */
    public boolean supportsWorldScoping() {
        return false;
    }

    /**
     * Gets the prefix for a player.
     * Thread-safe; safe to call from main thread.
     *
     * @param uuid The player UUID
     * @return The player's prefix, or empty string if none set
     */
    public abstract String getPlayerPrefix(UUID uuid);

    /**
     * Gets the prefix for a player in a specific world.
     * Default implementation ignores world; override for world-scoped prefixes.
     *
     * @param uuid The player UUID
     * @param world The world name
     * @return The player's prefix in that world, or empty string
     */
    public String getPlayerPrefix(UUID uuid, String world) {
        return getPlayerPrefix(uuid);
    }

    /**
     * Gets the prefix for a player asynchronously.
     *
     * @param uuid The player UUID
     * @return An async result with the player's prefix
     */
    public AsyncResult<String> getPlayerPrefixAsync(UUID uuid) {
        return executionProvider.supplyAsync(() -> getPlayerPrefix(uuid));
    }

    /**
     * Gets the suffix for a player.
     * Thread-safe; safe to call from main thread.
     *
     * @param uuid The player UUID
     * @return The player's suffix, or empty string if none set
     */
    public abstract String getPlayerSuffix(UUID uuid);

    /**
     * Gets the suffix for a player in a specific world.
     * Default implementation ignores world; override for world-scoped suffixes.
     *
     * @param uuid The player UUID
     * @param world The world name
     * @return The player's suffix in that world, or empty string
     */
    public String getPlayerSuffix(UUID uuid, String world) {
        return getPlayerSuffix(uuid);
    }

    /**
     * Gets the suffix for a player asynchronously.
     *
     * @param uuid The player UUID
     * @return An async result with the player's suffix
     */
    public AsyncResult<String> getPlayerSuffixAsync(UUID uuid) {
        return executionProvider.supplyAsync(() -> getPlayerSuffix(uuid));
    }

    /**
     * Gets a generic info node for a player (String value).
     *
     * @param uuid The player UUID
     * @param node The info node key
     * @return The info value, or null if not set
     */
    public String getPlayerInfoString(UUID uuid, String node) {
        throw new UnsupportedOperationException("Info nodes not supported by " + getName());
    }

    /**
     * Gets a generic info node for a player asynchronously.
     *
     * @param uuid The player UUID
     * @param node The info node key
     * @return An async result with the info value
     */
    public AsyncResult<String> getPlayerInfoStringAsync(UUID uuid, String node) {
        return executionProvider.supplyAsync(() -> getPlayerInfoString(uuid, node));
    }

    /**
     * Gets the prefix for a group.
     *
     * @param groupName The group name
     * @return The group's prefix, or empty string if none set
     */
    public String getGroupPrefix(String groupName) {
        throw new UnsupportedOperationException("Group prefixes not supported by " + getName());
    }

    /**
     * Gets the prefix for a group asynchronously.
     *
     * @param groupName The group name
     * @return An async result with the group's prefix
     */
    public AsyncResult<String> getGroupPrefixAsync(String groupName) {
        return executionProvider.supplyAsync(() -> getGroupPrefix(groupName));
    }

    /**
     * Gets the suffix for a group.
     *
     * @param groupName The group name
     * @return The group's suffix, or empty string if none set
     */
    public String getGroupSuffix(String groupName) {
        throw new UnsupportedOperationException("Group suffixes not supported by " + getName());
    }

    /**
     * Gets the suffix for a group asynchronously.
     *
     * @param groupName The group name
     * @return An async result with the group's suffix
     */
    public AsyncResult<String> getGroupSuffixAsync(String groupName) {
        return executionProvider.supplyAsync(() -> getGroupSuffix(groupName));
    }

    /**
     * Sets the prefix for a player (if supported).
     *
     * @param uuid The player UUID
     * @param prefix The prefix to set
     * @return true if successfully set
     */
    public boolean setPlayerPrefix(UUID uuid, String prefix) {
        throw new UnsupportedOperationException("Setting prefixes not supported by " + getName());
    }

    /**
     * Sets the prefix for a player asynchronously.
     *
     * @param uuid The player UUID
     * @param prefix The prefix to set
     * @return An async result with success status
     */
    public AsyncResult<Boolean> setPlayerPrefixAsync(UUID uuid, String prefix) {
        return executionProvider.supplyAsync(() -> setPlayerPrefix(uuid, prefix));
    }

    /**
     * Sets the suffix for a player (if supported).
     *
     * @param uuid The player UUID
     * @param suffix The suffix to set
     * @return true if successfully set
     */
    public boolean setPlayerSuffix(UUID uuid, String suffix) {
        throw new UnsupportedOperationException("Setting suffixes not supported by " + getName());
    }

    /**
     * Sets the suffix for a player asynchronously.
     *
     * @param uuid The player UUID
     * @param suffix The suffix to set
     * @return An async result with success status
     */
    public AsyncResult<Boolean> setPlayerSuffixAsync(UUID uuid, String suffix) {
        return executionProvider.supplyAsync(() -> setPlayerSuffix(uuid, suffix));
    }
}
