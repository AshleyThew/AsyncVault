# PlaceholderAPI Chat Provider Example

This is a full plugin-style example that exposes an AsyncVault chat provider using the real PlaceholderAPI static API.

## What this example includes

- A Spigot plugin bootstrap
- Direct PlaceholderAPI usage
- An AsyncVault chat provider
- Service registration on startup
- A consumer example showing how another plugin can use the provider

## Plugin Bootstrap

```java
import com.asyncvault.api.chat.ChatProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class AsyncVaultPlaceholderPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        ChatProvider chatProvider = new PlaceholderChatProvider();

        Bukkit.getServicesManager().register(ChatProvider.class, chatProvider, this, ServicePriority.Normal);
        getLogger().info("Registered AsyncVault PlaceholderAPI chat provider");
    }

    @Override
    public void onDisable() {
        Bukkit.getServicesManager().unregisterAll(this);
    }
}
```

## AsyncVault Provider

```java
import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.chat.ChatProvider;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public final class PlaceholderChatProvider extends ChatProvider {

    public PlaceholderChatProvider() {
        super();
    }

    @Override
    public String getName() {
        return "PlaceholderAPI";
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
        return getExecutionProvider().supplySync(() -> resolve(uuid, "%player_prefix%")).asAsync();
    }

    @Override
    public String getPlayerSuffix(UUID uuid) {
        throw new UnsupportedOperationException("Use getPlayerSuffixAsync");
    }

    @Override
    public AsyncResult<String> getPlayerSuffixAsync(UUID uuid) {
        return getExecutionProvider().supplySync(() -> resolve(uuid, "%player_suffix%")).asAsync();
    }

    private String resolve(UUID uuid, String placeholder) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }
}
```

## Consumer Example

```java
import com.asyncvault.api.chat.ChatProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class NameFormattingFeature {

    public void updateName(Player player) {
        ChatProvider chat = Bukkit.getServicesManager().load(ChatProvider.class);
        if (chat == null) {
            player.sendMessage("No AsyncVault chat provider is available.");
            return;
        }

        chat.getPlayerPrefixAsync(player.getUniqueId())
            .thenCombine(chat.getPlayerSuffixAsync(player.getUniqueId()), (prefix, suffix) -> prefix + player.getName() + suffix)
            .thenSync(formatted -> {
                player.setDisplayName(formatted);
                return null;
            });
    }
}
```

## Notes

- This example calls the real PlaceholderAPI resolver directly.
- The provider uses the AsyncVault execution provider to keep the integration consistent.
- Register the provider only after PlaceholderAPI is enabled and ready.
