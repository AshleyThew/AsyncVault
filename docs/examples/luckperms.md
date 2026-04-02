# LuckPerms Permission + Chat Provider Example

This is a full plugin-style example that exposes AsyncVault permission and chat providers using the real LuckPerms API.

## What this example includes

- A Spigot plugin bootstrap
- Direct LuckPerms API usage
- AsyncVault permission and chat providers
- Service registration on startup
- A consumer example showing how another plugin can read permissions and chat data

## Plugin Bootstrap

```java
import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.api.permission.PermissionProvider;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class AsyncVaultLuckPermsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        LuckPerms luckPerms = LuckPermsProvider.get();

        PermissionProvider permissionProvider = new LuckPermsPermissionProvider(luckPerms);
        ChatProvider chatProvider = new LuckPermsChatProvider(luckPerms);

        Bukkit.getServicesManager().register(PermissionProvider.class, permissionProvider, this, ServicePriority.Normal);
        Bukkit.getServicesManager().register(ChatProvider.class, chatProvider, this, ServicePriority.Normal);

        getLogger().info("Registered AsyncVault LuckPerms providers");
    }

    @Override
    public void onDisable() {
        Bukkit.getServicesManager().unregisterAll(this);
    }
}
```

## AsyncVault Permission Provider

```java
import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.permission.PermissionProvider;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

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

            result.complete(hasPermission(user, permission));
        });
        return result;
    }

    private boolean hasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}
```

## AsyncVault Chat Provider

```java
import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.chat.ChatProvider;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public final class LuckPermsChatProvider extends ChatProvider {

    private final LuckPerms luckPerms;

    public LuckPermsChatProvider(LuckPerms luckPerms) {
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
    public String getPlayerPrefix(UUID uuid) {
        throw new UnsupportedOperationException("Use getPlayerPrefixAsync");
    }

    @Override
    public AsyncResult<String> getPlayerPrefixAsync(UUID uuid) {
        AsyncResult<String> result = AsyncResult.create(getExecutionProvider());
        luckPerms.getUserManager().loadUser(uuid).whenComplete((user, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }

            result.complete(prefix(user));
        });
        return result;
    }

    @Override
    public String getPlayerSuffix(UUID uuid) {
        throw new UnsupportedOperationException("Use getPlayerSuffixAsync");
    }

    @Override
    public AsyncResult<String> getPlayerSuffixAsync(UUID uuid) {
        AsyncResult<String> result = AsyncResult.create(getExecutionProvider());
        luckPerms.getUserManager().loadUser(uuid).whenComplete((user, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }

            result.complete(suffix(user));
        });
        return result;
    }

    private String prefix(User user) {
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix == null ? "" : prefix;
    }

    private String suffix(User user) {
        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix == null ? "" : suffix;
    }
}
```

## Consumer Example

```java
import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.api.permission.PermissionProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ChatAndPermissionFeature {

    public void formatName(Player player) {
        PermissionProvider permission = Bukkit.getServicesManager().load(PermissionProvider.class);
        ChatProvider chat = Bukkit.getServicesManager().load(ChatProvider.class);

        if (permission == null || chat == null) {
            player.sendMessage("AsyncVault providers are missing.");
            return;
        }

        permission.hasPermissionAsync(player.getUniqueId(), "myplugin.vip")
            .thenCombine(chat.getPlayerPrefixAsync(player.getUniqueId()), (hasVip, prefix) -> {
                String decorated = prefix + player.getName();
                return hasVip ? "[VIP] " + decorated : decorated;
            })
            .thenSync(display -> {
                player.setDisplayName(display);
                return null;
            });
    }
}
```

## Notes

- This example uses the real LuckPerms async user loading API.
- Keep permission checks and chat lookups async.
- Register both providers from the same plugin bootstrap.
