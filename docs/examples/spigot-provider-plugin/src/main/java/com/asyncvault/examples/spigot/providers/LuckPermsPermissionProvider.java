package com.asyncvault.examples.spigot.providers;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.permission.PermissionProvider;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

import java.util.UUID;

public final class LuckPermsPermissionProvider extends PermissionProvider {

    private final LuckPerms luckPerms;

    public LuckPermsPermissionProvider(LuckPerms luckPerms) {
        super();
        this.luckPerms = luckPerms;
    }

    @Override
    public String getName() {
        return "LuckPerms";
    }

    @Override
    public boolean supportsAsyncOperations() {
        return true;
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        throw new UnsupportedOperationException("Use hasPermissionAsync");
    }

    @Override
    public AsyncResult<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        AsyncResult<Boolean> result = AsyncResult.create(getExecutionProvider());
        luckPerms.getUserManager().loadUser(uuid).whenComplete((user, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }
            result.complete(user != null && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean());
        });
        return result;
    }

    @Override
    public boolean grantPermission(UUID uuid, String permission) {
        throw new UnsupportedOperationException("Use grantPermissionAsync");
    }

    @Override
    public AsyncResult<Boolean> grantPermissionAsync(UUID uuid, String permission) {
        AsyncResult<Boolean> result = AsyncResult.create(getExecutionProvider());
        luckPerms.getUserManager().loadUser(uuid).whenComplete((user, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }
            if (user == null) {
                result.complete(false);
                return;
            }

            user.data().add(Node.builder(permission).build());
            luckPerms.getUserManager().saveUser(user).whenComplete((saved, saveError) -> {
                if (saveError != null) {
                    result.completeExceptionally(saveError);
                    return;
                }
                result.complete(true);
            });
        });
        return result;
    }

    @Override
    public boolean revokePermission(UUID uuid, String permission) {
        throw new UnsupportedOperationException("Use revokePermissionAsync");
    }

    @Override
    public AsyncResult<Boolean> revokePermissionAsync(UUID uuid, String permission) {
        AsyncResult<Boolean> result = AsyncResult.create(getExecutionProvider());
        luckPerms.getUserManager().loadUser(uuid).whenComplete((user, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }
            if (user == null) {
                result.complete(false);
                return;
            }

            user.data().remove(Node.builder(permission).build());
            luckPerms.getUserManager().saveUser(user).whenComplete((saved, saveError) -> {
                if (saveError != null) {
                    result.completeExceptionally(saveError);
                    return;
                }
                result.complete(true);
            });
        });
        return result;
    }
}
