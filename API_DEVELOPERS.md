# AsyncVault API Developer Guide

This guide explains how plugin developers can consume AsyncVault providers.

## 1. Add Dependency

```gradle
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.AshleyThew:AsyncVault:{version}:asyncvault-api")
}
```

## 2. Resolve Providers

Use your platform's service registry to resolve implementations of:

- `com.asyncvault.api.economy.EconomyProvider`
- `com.asyncvault.api.permission.PermissionProvider`
- `com.asyncvault.api.chat.ChatProvider`

If a provider is not present, treat it as optional and disable related plugin features gracefully.

### Spigot Lookup Example

```java
import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.permission.PermissionProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<EconomyProvider> ecoReg =
    Bukkit.getServicesManager().getRegistration(EconomyProvider.class);
RegisteredServiceProvider<PermissionProvider> permReg =
    Bukkit.getServicesManager().getRegistration(PermissionProvider.class);
RegisteredServiceProvider<ChatProvider> chatReg =
    Bukkit.getServicesManager().getRegistration(ChatProvider.class);

EconomyProvider economy = ecoReg != null ? ecoReg.getProvider() : null;
PermissionProvider permission = permReg != null ? permReg.getProvider() : null;
ChatProvider chat = chatReg != null ? chatReg.getProvider() : null;
```

### Sponge Lookup Example

```java
import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.permission.PermissionProvider;
import org.spongepowered.api.Sponge;

EconomyProvider economy = Sponge.server().serviceProvider().provide(EconomyProvider.class).orElse(null);
PermissionProvider permission = Sponge.server().serviceProvider().provide(PermissionProvider.class).orElse(null);
ChatProvider chat = Sponge.server().serviceProvider().provide(ChatProvider.class).orElse(null);
```

### Fabric Lookup Example

Fabric has no unified services manager. Read provider instances from the provider mod API/registry.

```java
EconomyProvider economy = MyProviderRegistry.economy();
PermissionProvider permission = MyProviderRegistry.permission();
ChatProvider chat = MyProviderRegistry.chat();
```

## 3. Result Chaining Model

AsyncVault exposes two concrete result types:

- `AsyncResult<T>` for async-first chains
- `SyncResult<T>` for sync-first chains

Rule of thumb:

- Use `AsyncResult.then(...)`, `thenAccept(...)`, `thenRun(...)`, `thenCompose(...)`, `handle(...)`, `whenComplete(...)`, and `thenCombine(...)` when you want the next step to stay on the async executor.
- Use the matching `...Sync(...)` methods when the next step must run on the sync executor.
- `SyncResult` inverts that rule: `then(...)` is sync, and `thenAsync(...)` / `thenAcceptAsync(...)` / `thenCombineAsync(...)` are the async variants.

## 4. Economy Usage (Async-Only)

Economy operations are async-only. Do not block the server thread.

```java
import com.asyncvault.api.economy.EconomyProvider;
import java.math.BigDecimal;
import java.util.UUID;

public final class EconomyFeature {

    private final EconomyProvider economy;

    public EconomyFeature(EconomyProvider economy) {
        this.economy = economy;
    }

    public void showBalance(UUID playerId) {
        economy.getBalanceAsync(playerId)
            .thenSync(balance -> {
                // Safe for server-thread interaction
                sendMessage(playerId, "Balance: " + economy.format(balance));
                return null;
            })
            .exceptionally(error -> {
                logError("Failed to load balance", error);
                return null;
            });
    }

    public void pay(UUID playerId, BigDecimal amount) {
        economy.withdrawAsync(playerId, amount)
            .thenSync(response -> {
                if (response.isSuccessful()) {
                    sendMessage(playerId, "Paid " + economy.format(amount));
                } else {
                    sendMessage(playerId, "Payment failed: " + response.getErrorMessage());
                }
                return null;
            })
            .exceptionally(error -> {
                logError("Payment exception", error);
                return null;
            });
    }

    private void sendMessage(UUID playerId, String message) {
        // Implement with your platform API
    }

    private void logError(String message, Throwable error) {
        // Implement with your logger
    }
}
```

## 5. Permission Usage

```java
import com.asyncvault.api.permission.PermissionProvider;

permission.hasPermissionAsync(playerId, "myplugin.command.use")
    .thenSync(has -> {
        if (!has) {
            denyCommand(playerId);
        }
        return null;
    });
```

For write operations:

```java
permission.grantPermissionAsync(playerId, "myplugin.vip")
    .thenSync(success -> {
        if (success) {
            notifyGranted(playerId);
        }
        return null;
    });
```

## 6. Chat Usage

```java
import com.asyncvault.api.chat.ChatProvider;

chat.getPlayerPrefixAsync(playerId)
    .thenCombine(chat.getPlayerSuffixAsync(playerId), (prefix, suffix) -> prefix + " " + suffix)
    .thenSync(format -> {
        updateDisplayName(playerId, format);
        return null;
    });
```

## 7. Error Handling

Use one of these patterns consistently:

- `exceptionally(...)` / `exceptionallySync(...)` for fallback values
- `handle(...)` / `handleSync(...)` for success + failure in one chain
- `whenComplete(...)` / `whenCompleteSync(...)` for side-effects and logging

Example:

```java
import java.math.BigDecimal;

economy.depositAsync(playerId, BigDecimal.valueOf(100L))
    .handleSync((response, error) -> {
        if (error != null) {
            logError("Deposit failed with exception", error);
            return null;
        }

        if (!response.isSuccessful()) {
            logWarn("Deposit rejected: " + response.getErrorMessage());
            return null;
        }

        onDepositSuccess(playerId, response.getAmount());
        return null;
    });
```

## 8. Threading Rules

- Use `then(...)` on `AsyncResult` for CPU/IO post-processing.
- Use `thenSync(...)` on `AsyncResult` before calling server APIs that require the main thread.
- Use `thenAsync(...)` on `SyncResult` when you need to hop back off the main thread.
- Never call blocking methods like `get()` on the server thread.

## 9. Defensive Integration Checklist

- Check for missing providers at startup.
- Disable only the feature that depends on a missing provider.
- Treat provider failures as runtime events, not fatal plugin crashes.
- Keep all economy flow asynchronous end-to-end.
