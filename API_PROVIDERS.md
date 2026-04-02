# AsyncVault Provider Implementation Guide

This guide shows practical provider implementations with real async operations.

## Add Dependency

```gradle
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.AshleyThew:AsyncVault:{version}:asyncvault-api")
}
```

## Key Rules

1. Plugin loaders provide `ExecutionProvider` through context.
2. Use the provider no-arg constructor and call `super()`.
3. Never block the server thread with database I/O.
4. Use `getExecutionProvider().supplySync(...)` for work that must run on the sync executor, then convert with `.asAsync()` if the provider method returns `AsyncResult`.
5. Use `getExecutionProvider().supply(...)` or `supplyAsync(...)` for async work.

Result chaining rule:

- `AsyncResult` uses `then(...)` for async follow-up and `thenSync(...)` for sync follow-up.
- `SyncResult` uses `then(...)` for sync follow-up and `thenAsync(...)` for async follow-up.

If you have a custom bootstrap or tests, explicit `ExecutionProvider` injection is still valid.

## Example: Economy Provider (SQL-backed)

```java
import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.economy.EconomyResponse;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public final class SqlEconomyProvider extends EconomyProvider {

    private final DataSource dataSource;

    public SqlEconomyProvider(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    @Override
    public String getName() {
        return "SqlEconomy";
    }

    @Override
    public boolean supportsAsyncOperations() {
        return true;
    }

    @Override
    public AsyncResult<BigDecimal> getBalanceAsync(UUID uuid) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT balance FROM balances WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to fetch balance", ex);
            }
        });
    }

    @Override
    public AsyncResult<EconomyResponse> depositAsync(UUID uuid, BigDecimal amount) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection()) {
                con.setAutoCommit(false);

                BigDecimal current;
                try (PreparedStatement read = con.prepareStatement("SELECT balance FROM balances WHERE uuid = ? FOR UPDATE")) {
                    read.setString(1, uuid.toString());
                    try (ResultSet rs = read.executeQuery()) {
                        current = rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
                    }
                }

                BigDecimal updated = current.add(amount);
                try (PreparedStatement upsert = con.prepareStatement(
                    "MERGE INTO balances (uuid, balance) KEY(uuid) VALUES (?, ?)")) {
                    upsert.setString(1, uuid.toString());
                    upsert.setBigDecimal(2, updated);
                    upsert.executeUpdate();
                }

                con.commit();
                return EconomyResponse.success(updated);
            } catch (Exception ex) {
                return EconomyResponse.failure("Deposit failed: " + ex.getMessage());
            }
        });
    }

    @Override
    public AsyncResult<EconomyResponse> withdrawAsync(UUID uuid, BigDecimal amount) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection()) {
                con.setAutoCommit(false);

                BigDecimal current;
                try (PreparedStatement read = con.prepareStatement("SELECT balance FROM balances WHERE uuid = ? FOR UPDATE")) {
                    read.setString(1, uuid.toString());
                    try (ResultSet rs = read.executeQuery()) {
                        current = rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
                    }
                }

                if (current.compareTo(amount) < 0) {
                    return EconomyResponse.insufficientFunds(current, amount);
                }

                BigDecimal updated = current.subtract(amount);
                try (PreparedStatement write = con.prepareStatement("UPDATE balances SET balance = ? WHERE uuid = ?")) {
                    write.setBigDecimal(1, updated);
                    write.setString(2, uuid.toString());
                    write.executeUpdate();
                }

                con.commit();
                return EconomyResponse.success(updated);
            } catch (Exception ex) {
                return EconomyResponse.failure("Withdraw failed: " + ex.getMessage());
            }
        });
    }

    @Override
    public AsyncResult<Boolean> hasAccountAsync(UUID uuid) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT 1 FROM balances WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to check account", ex);
            }
        });
    }
}
```

## Example: Permission Provider (DB-backed)

```java
import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.permission.PermissionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public final class SqlPermissionProvider extends PermissionProvider {

    private final DataSource dataSource;

    public SqlPermissionProvider(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    @Override
    public String getName() {
        return "SqlPermission";
    }

    @Override
    public boolean supportsAsyncOperations() {
        return true;
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        throw new UnsupportedOperationException("Use hasPermissionAsync for DB-backed provider");
    }

    @Override
    public AsyncResult<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM permissions WHERE uuid = ? AND node = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, permission);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (Exception ex) {
                throw new RuntimeException("Permission lookup failed", ex);
            }
        });
    }

    @Override
    public boolean grantPermission(UUID uuid, String permission) {
        throw new UnsupportedOperationException("Use grantPermissionAsync for DB-backed provider");
    }

    @Override
    public AsyncResult<Boolean> grantPermissionAsync(UUID uuid, String permission) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO permissions(uuid, node) VALUES(?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, permission);
                return ps.executeUpdate() > 0;
            } catch (Exception ex) {
                return false;
            }
        });
    }

    @Override
    public boolean revokePermission(UUID uuid, String permission) {
        throw new UnsupportedOperationException("Use revokePermissionAsync for DB-backed provider");
    }

    @Override
    public AsyncResult<Boolean> revokePermissionAsync(UUID uuid, String permission) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM permissions WHERE uuid = ? AND node = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, permission);
                return ps.executeUpdate() > 0;
            } catch (Exception ex) {
                return false;
            }
        });
    }
}
```

## Example: Chat Provider (DB-backed)

```java
import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.chat.ChatProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public final class SqlChatProvider extends ChatProvider {

    private final DataSource dataSource;

    public SqlChatProvider(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    @Override
    public String getName() {
        return "SqlChat";
    }

    @Override
    public boolean supportsAsyncOperations() {
        return true;
    }

    @Override
    public String getPlayerPrefix(UUID uuid) {
        throw new UnsupportedOperationException("Use getPlayerPrefixAsync for DB-backed provider");
    }

    @Override
    public AsyncResult<String> getPlayerPrefixAsync(UUID uuid) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "SELECT prefix FROM chat_meta WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("prefix") : "";
                }
            } catch (Exception ex) {
                return "";
            }
        });
    }

    @Override
    public String getPlayerSuffix(UUID uuid) {
        throw new UnsupportedOperationException("Use getPlayerSuffixAsync for DB-backed provider");
    }

    @Override
    public AsyncResult<String> getPlayerSuffixAsync(UUID uuid) {
        return getExecutionProvider().supplyAsync(() -> {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "SELECT suffix FROM chat_meta WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("suffix") : "";
                }
            } catch (Exception ex) {
                return "";
            }
        });
    }
}
```

## Registering Providers

Register providers with your platform-native service registry/event system.

Construct providers only after AsyncVault has initialized execution context for your platform lifecycle.

### Spigot (ServicesManager)

```java
import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.permission.PermissionProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyProviderPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        EconomyProvider economy = new SqlEconomyProvider(createDataSource());
        PermissionProvider permission = new SqlPermissionProvider(createDataSource());
        ChatProvider chat = new SqlChatProvider(createDataSource());

        Bukkit.getServicesManager().register(EconomyProvider.class, economy, this, ServicePriority.Normal);
        Bukkit.getServicesManager().register(PermissionProvider.class, permission, this, ServicePriority.Normal);
        Bukkit.getServicesManager().register(ChatProvider.class, chat, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        Bukkit.getServicesManager().unregisterAll(this);
    }
}
```

### Sponge (ServiceProvider)

```java
import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.permission.PermissionProvider;
import org.spongepowered.api.Sponge;

public final class MyProviderBootstrap {

    public void registerProviders() {
        EconomyProvider economy = new SqlEconomyProvider(createDataSource());
        PermissionProvider permission = new SqlPermissionProvider(createDataSource());
        ChatProvider chat = new SqlChatProvider(createDataSource());

        Sponge.server().serviceProvider().provideUnchecked(EconomyProvider.class, economy);
        Sponge.server().serviceProvider().provideUnchecked(PermissionProvider.class, permission);
        Sponge.server().serviceProvider().provideUnchecked(ChatProvider.class, chat);
    }
}
```

### Fabric (mod-owned registry)

Fabric does not provide a built-in cross-mod services manager equivalent to Spigot/Sponge.
Expose your provider instances from your mod API (singleton, accessor, or entrypoint interface).

```java
public final class MyProviderRegistry {
    private static EconomyProvider economy;
    private static PermissionProvider permission;
    private static ChatProvider chat;

    public static void register(EconomyProvider eco, PermissionProvider perm, ChatProvider c) {
        economy = eco;
        permission = perm;
        chat = c;
    }

    public static EconomyProvider economy() {
        return economy;
    }

    public static PermissionProvider permission() {
        return permission;
    }

    public static ChatProvider chat() {
        return chat;
    }
}
```
