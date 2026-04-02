package com.asyncvault.examples.spigot.providers;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.economy.EconomyResponse;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.Economy;

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
        return getExecutionProvider().supplySync(() -> {
            try {
                return Economy.getMoneyExact(uuid);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to fetch EssentialsX balance", ex);
            }
        }).asAsync();
    }

    @Override
    public AsyncResult<EconomyResponse> depositAsync(UUID uuid, BigDecimal amount) {
        return getExecutionProvider().supplySync(() -> {
            try {
                Economy.add(uuid, amount);
                return EconomyResponse.success(amount);
            } catch (Exception ex) {
                return EconomyResponse.failure("Deposit failed: " + ex.getMessage());
            }
        }).asAsync();
    }

    @Override
    public AsyncResult<EconomyResponse> withdrawAsync(UUID uuid, BigDecimal amount) {
        return getExecutionProvider().supplySync(() -> {
            try {
                User user = essentials.getUser(uuid);
                if (user == null) {
                    return EconomyResponse.accountNotFound();
                }

                BigDecimal balance = Economy.getMoneyExact(user);
                if (balance.compareTo(amount) < 0) {
                    return EconomyResponse.insufficientFunds(balance, amount);
                }

                Economy.subtract(uuid, amount);
                return EconomyResponse.success(amount);
            } catch (Exception ex) {
                return EconomyResponse.failure("Withdraw failed: " + ex.getMessage());
            }
        }).asAsync();
    }

    @Override
    public AsyncResult<Boolean> hasAccountAsync(UUID uuid) {
        return getExecutionProvider().supplySync(() -> essentials.getUser(uuid) != null).asAsync();
    }

    @Override
    public String getCurrency() {
        return "$";
    }
}
