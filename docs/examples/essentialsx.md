# EssentialsX Economy Provider Example

This is a full plugin-style example that exposes an AsyncVault economy provider using the real EssentialsX economy API directly.

## What this example includes

- A Spigot plugin bootstrap
- Direct EssentialsX plugin and economy API usage
- An AsyncVault economy provider with `BigDecimal` values
- Economy capability flags (world, bank, multi-currency)
- Provider registration on startup
- A consumer example showing how another plugin can use the provider

## Plugin Bootstrap

```java
import com.asyncvault.api.economy.EconomyProvider;
import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class AsyncVaultEssentialsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Essentials essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null || !essentials.isEnabled()) {
            getLogger().severe("Essentials is not enabled, so AsyncVault economy cannot start.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        EconomyProvider economyProvider = new EssentialsEconomyProvider(essentials);

        Bukkit.getServicesManager().register(EconomyProvider.class, economyProvider, this, ServicePriority.Normal);
        getLogger().info("Registered AsyncVault economy provider backed by EssentialsX");
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
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.economy.EconomyResponse;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.Economy;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.UUID;

public final class EssentialsEconomyProvider extends EconomyProvider {

    private final Essentials essentials;

    public EssentialsEconomyProvider(Essentials essentials) {
        super();
        this.essentials = essentials;
    }

    @Override
    public String getName() {
        return "EssentialsX";
    }

    @Override
    public boolean supportsAsyncOperations() {
        return true;
    }

    @Override
    public boolean supportsWorldScoping() {
        return false;
    }

    @Override
    public boolean supportsBankAccounts() {
        return false;
    }

    @Override
    public boolean supportsMultipleCurrencies() {
        return false;
    }

    @Override
    public AsyncResult<BigDecimal> getBalanceAsync(UUID uuid) {
        return getExecutionProvider().supplySync(() -> Economy.getMoneyExact(uuid)).asAsync();
    }

    @Override
    public AsyncResult<EconomyResponse> depositAsync(UUID uuid, BigDecimal amount) {
        return getExecutionProvider().supplySync(() -> {
            Economy.add(uuid, amount);
            return EconomyResponse.success(amount);
        }).asAsync();
    }

    @Override
    public AsyncResult<EconomyResponse> withdrawAsync(UUID uuid, BigDecimal amount) {
        return getExecutionProvider().supplySync(() -> {
            User user = essentials.getUser(uuid);
            if (user == null) {
                return EconomyResponse.accountNotFound();
            }

            if (!user.canAfford(amount)) {
                return EconomyResponse.insufficientFunds(Economy.getMoneyExact(user), amount);
            }

            Economy.subtract(uuid, amount);
            return EconomyResponse.success(amount);
        }).asAsync();
    }

    @Override
    public AsyncResult<Boolean> hasAccountAsync(UUID uuid) {
        return getExecutionProvider().supplySync(() -> essentials.getUser(uuid) != null).asAsync();
    }
}
```

## Consumer Example

```java
import com.asyncvault.api.economy.EconomyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

public final class BalanceCommandPlugin extends JavaPlugin {

    public void showBalance(Player player) {
        RegisteredServiceProvider<EconomyProvider> registration =
            Bukkit.getServicesManager().getRegistration(EconomyProvider.class);

        if (registration == null) {
            player.sendMessage("No AsyncVault economy provider is available.");
            return;
        }

        EconomyProvider economy = registration.getProvider();
        economy.getBalanceAsync(player.getUniqueId())
            .thenSync(balance -> {
                player.sendMessage("Balance: " + economy.format(balance));
                return null;
            });
    }

    public void charge(Player player, BigDecimal amount) {
        EconomyProvider economy = Bukkit.getServicesManager().load(EconomyProvider.class);
        if (economy == null) {
            return;
        }

        economy.withdrawAsync(player.getUniqueId(), amount)
            .thenSync(response -> {
                if (!response.isSuccessful()) {
                    player.sendMessage("Charge failed: " + response.getErrorMessage());
                }
                return null;
            });
    }
}
```

## Scoped And Multi-Currency Support

Use capability flags before resolving scoped providers:

```java
if (economy.supportsWorldScoping()) {
    EconomyProvider netherEconomy = economy.getWorldScopedProvider("world_nether");
}

if (economy.supportsBankAccounts()) {
    EconomyProvider guildBank = economy.getBankAccountProvider("guild:alpha");
}

if (economy.supportsMultipleCurrencies()) {
    for (String currencyId : economy.getSupportedCurrencies()) {
        EconomyProvider currencyProvider = economy.getCurrencyProvider(currencyId);
        // Use the currency-specific provider for balance/withdraw/deposit calls.
    }
}
```

## Notes

- This example uses the real EssentialsX API directly, not Vault.
- Keep money amounts as `BigDecimal` end to end.
- Register the provider only after Essentials is enabled.
- EssentialsX in this example is single-currency and not world/bank scoped.
