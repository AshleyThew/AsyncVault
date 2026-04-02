package com.asyncvault.api.permission;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;

import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for permission service implementations.
 *
 * <p>Supports both sync and async permission checks. Implementers should override
 * async methods for true async behavior; default wraps sync calls.
 */
public abstract class PermissionProvider {

    private final ExecutionProvider executionProvider;

    protected PermissionProvider() {
        this(ExecutionProviderContext.require());
    }

    protected PermissionProvider(ExecutionProvider executionProvider) {
        this.executionProvider = Objects.requireNonNull(executionProvider, "executionProvider");
    }

    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }

    /**
     * @return The name of the permission backend (e.g., "LuckPerms", "SuperPerms")
     */
    public abstract String getName();

    /**
     * @return true if this provider supports asynchronous operations natively
     */
    public abstract boolean supportsAsyncOperations();

    /**
     * @return true if this provider supports groups/roles
     */
    public boolean supportsGroupManagement() {
        return false;
    }

    /**
     * Checks if a player has a permission.
     * Thread-safe; safe to call from main thread.
     *
     * @param uuid The player UUID
     * @param permission The permission node (e.g., "myapp.command.reload")
     * @return true if player has the permission
     */
    public abstract boolean hasPermission(UUID uuid, String permission);

    /**
     * Checks if a player has a permission in a specific world.
     * Default implementation ignores world; override for world-scoped permissions.
     *
     * @param uuid The player UUID
     * @param world The world name
     * @param permission The permission node
     * @return true if player has the permission in that world
     */
    public boolean hasPermission(UUID uuid, String world, String permission) {
        return hasPermission(uuid, permission);
    }

    /**
     * Checks if a player has a permission (legacy String-based, pre-UUID servers).
     * Default throws UnsupportedOperationException; override for servers without UUID support.
     *
     * @param playerName The player name
     * @param permission The permission node
     * @return true if player has the permission
     * @deprecated Use UUID-based method instead
     */
    @Deprecated
    public boolean hasPermission(String playerName, String permission) {
        throw new UnsupportedOperationException("String-based player identification not supported");
    }

    /**
     * Checks if a player has a permission asynchronously.
     *
     * <p>The result is completed on an async thread. If you need to interact with
     * Bukkit API, schedule callback on main thread before using result.
     *
     * @param uuid The player UUID
     * @param permission The permission node
     * @return An async result with the permission check result
     */
    public AsyncResult<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        return executionProvider.supplyAsync(() -> hasPermission(uuid, permission));
    }

    /**
     * Checks if a player has a permission asynchronously in a specific world.
     *
     * @param uuid The player UUID
     * @param world The world name
     * @param permission The permission node
     * @return An async result with the permission check result
     */
    public AsyncResult<Boolean> hasPermissionAsync(UUID uuid, String world, String permission) {
        return executionProvider.supplyAsync(() -> hasPermission(uuid, world, permission));
    }

    /**
     * Grants a permission to a player (if supported).
     * Thread-safe; safe to call from main thread.
     *
     * @param uuid The player UUID
     * @param permission The permission node
     * @return true if successfully granted
     * @throws UnsupportedOperationException if not supported by backend
     */
    public abstract boolean grantPermission(UUID uuid, String permission);

    /**
     * Grants a permission asynchronously.
     *
     * @param uuid The player UUID
     * @param permission The permission node
     * @return An async result with success status
     */
    public AsyncResult<Boolean> grantPermissionAsync(UUID uuid, String permission) {
        return executionProvider.supplyAsync(() -> grantPermission(uuid, permission));
    }

    /**
     * Revokes a permission from a player (if supported).
     * Thread-safe; safe to call from main thread.
     *
     * @param uuid The player UUID
     * @param permission The permission node
     * @return true if successfully revoked
     * @throws UnsupportedOperationException if not supported by backend
     */
    public abstract boolean revokePermission(UUID uuid, String permission);

    /**
     * Revokes a permission asynchronously.
     *
     * @param uuid The player UUID
     * @param permission The permission node
     * @return An async result with success status
     */
    public AsyncResult<Boolean> revokePermissionAsync(UUID uuid, String permission) {
        return executionProvider.supplyAsync(() -> revokePermission(uuid, permission));
    }

    /**
     * Gets the primary group of a player (if supported).
     *
     * @param uuid The player UUID
     * @return The group name, or null if not supported or player has no group
     */
    public String getPrimaryGroup(UUID uuid) {
        throw new UnsupportedOperationException("Group retrieval not supported by " + getName());
    }

    /**
     * Gets the primary group of a player asynchronously (if supported).
     *
     * @param uuid The player UUID
     * @return An async result with the group name
     */
    public AsyncResult<String> getPrimaryGroupAsync(UUID uuid) {
        return executionProvider.supplyAsync(() -> getPrimaryGroup(uuid));
    }

    /**
     * Adds a player to a group (if supported).
     *
     * @param uuid The player UUID
     * @param groupName The group name
     * @return true if successfully added
     * @throws UnsupportedOperationException if not supported
     */
    public boolean addToGroup(UUID uuid, String groupName) {
        throw new UnsupportedOperationException("Group management not supported by " + getName());
    }

    /**
     * Adds a player to a group asynchronously.
     *
     * @param uuid The player UUID
     * @param groupName The group name
     * @return An async result with success status
     */
    public AsyncResult<Boolean> addToGroupAsync(UUID uuid, String groupName) {
        return executionProvider.supplyAsync(() -> addToGroup(uuid, groupName));
    }

    /**
     * Removes a player from a group (if supported).
     *
     * @param uuid The player UUID
     * @param groupName The group name
     * @return true if successfully removed
     * @throws UnsupportedOperationException if not supported
     */
    public boolean removeFromGroup(UUID uuid, String groupName) {
        throw new UnsupportedOperationException("Group management not supported by " + getName());
    }

    /**
     * Removes a player from a group asynchronously.
     *
     * @param uuid The player UUID
     * @param groupName The group name
     * @return An async result with success status
     */
    public AsyncResult<Boolean> removeFromGroupAsync(UUID uuid, String groupName) {
        return executionProvider.supplyAsync(() -> removeFromGroup(uuid, groupName));
    }
}
