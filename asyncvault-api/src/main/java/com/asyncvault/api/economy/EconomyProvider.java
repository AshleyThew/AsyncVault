package com.asyncvault.api.economy;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Abstract base class for economy service implementations.
 *
 * <p>Economy operations are async-only. Implementations must provide async methods
 * for all account and transaction operations.
 *
 * <p>For UUID: Modern implementations (Java 17+) use UUID exclusively.
 * For backwards compatibility with older servers, implementations may override
 * the String-based methods to support offline/string-based player identification.
 */
public abstract class EconomyProvider {

    private final ExecutionProvider executionProvider;

    protected EconomyProvider() {
        this(ExecutionProviderContext.require());
    }

    protected EconomyProvider(ExecutionProvider executionProvider) {
        this.executionProvider = Objects.requireNonNull(executionProvider, "executionProvider");
    }

    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }

    /**
     * @return The name of the economy backend (e.g., "Essentials", "Economy")
     */
    public abstract String getName();

    /**
     * @return true if this provider supports asynchronous operations natively
     */
    public abstract boolean supportsAsyncOperations();

    /**
     * @return true if this provider supports world-scoped balances
     */
    public boolean supportsWorldScoping() {
        return false;
    }

    /**
     * Gets the provider instance that handles world-scoped operations.
     *
     * @return provider to use for world-scoped economy operations
     */
    public EconomyProvider getWorldScopedProvider() {
        return this;
    }

    /**
     * Gets a provider instance scoped to a specific world.
     *
     * @param worldName The world name to scope to
     * @return world-scoped provider instance
     * @throws UnsupportedOperationException if world scoping is not supported
     */
    public WorldEconomyProvider getWorldScopedProvider(String worldName) {
        throw new UnsupportedOperationException("World scoping not supported by " + getName());
    }

    /**
     * @return true if this provider supports bank accounts
     */
    public boolean supportsBankAccounts() {
        return false;
    }

    /**
     * Gets the provider instance that handles bank account operations.
     *
     * @return provider to use for bank account economy operations
     */
    public EconomyProvider getBankAccountProvider() {
        return this;
    }

    /**
     * Gets a provider instance scoped to a specific bank account id.
     *
     * @param bankAccountId The bank account id to scope to
     * @return bank account-scoped provider instance
     * @throws UnsupportedOperationException if bank accounts are not supported
     */
    public BankAccountEconomyProvider getBankAccountProvider(String bankAccountId) {
        throw new UnsupportedOperationException("Bank accounts not supported by " + getName());
    }

    /**
     * @return true if this provider supports multiple currencies
     */
    public boolean supportsMultipleCurrencies() {
        return false;
    }

    /**
     * Lists currencies supported by this provider.
     *
     * @return supported currency identifiers
     */
    public List<String> getSupportedCurrencies() {
        return Collections.singletonList(getCurrency());
    }

    /**
     * Gets the provider for a specific currency.
     *
     * @param currencyId The currency identifier
     * @return provider to use for that currency
     * @throws UnsupportedOperationException if the requested currency is unsupported
     */
    public EconomyProvider getCurrencyProvider(String currencyId) {
        if (Objects.equals(getCurrency(), currencyId)) {
            return this;
        }
        throw new UnsupportedOperationException("Currency '" + currencyId + "' not supported by " + getName());
    }

    /**
     * Gets providers for all supported currencies.
     *
     * @return providers for all supported currencies
     */
    public List<EconomyProvider> getCurrencyProviders() {
        return getSupportedCurrencies().stream()
            .map(this::getCurrencyProvider)
            .collect(Collectors.toList());
    }

    /**
     * Gets balance asynchronously.
     *
     * <p>The result is completed on an async thread. If you need to interact with
     * Bukkit API, use {@link com.asyncvault.api.AsyncResult#thenAcceptSync(java.util.function.Consumer)}
     * to safely schedule callback on main thread.
     *
     * @param uuid The player UUID
     * @return An async result that completes with the balance
     */
    public abstract AsyncResult<BigDecimal> getBalanceAsync(UUID uuid);

    /**
     * Gets balance asynchronously for a specific world.
     *
     * @param uuid The player UUID
     * @param world The world name
     * @return An async result that completes with the balance
     */
    public AsyncResult<BigDecimal> getBalanceAsync(UUID uuid, String world) {
        return getBalanceAsync(uuid);
    }

    /**
     * Gets balance asynchronously (legacy String-based, pre-UUID servers).
     *
     * @param playerName The player name
     * @return An async result that completes with the balance
     * @deprecated Use UUID-based method instead
     */
    @Deprecated
    public AsyncResult<BigDecimal> getBalanceAsync(String playerName) {
        throw new UnsupportedOperationException("String-based player identification not supported");
    }

    /**
     * Deposits money asynchronously.
     *
     * @param uuid The player UUID
     * @param amount The amount to deposit
     * @return An async result with the response
     */
    public abstract AsyncResult<EconomyResponse> depositAsync(UUID uuid, BigDecimal amount);

    /**
     * Deposits money asynchronously (legacy String-based, pre-UUID servers).
     *
     * @param playerName The player name
     * @param amount The amount to deposit
     * @return An async result with the response
     * @deprecated Use UUID-based method instead
     */
    @Deprecated
    public AsyncResult<EconomyResponse> depositAsync(String playerName, BigDecimal amount) {
        throw new UnsupportedOperationException("String-based player identification not supported");
    }

    /**
     * Withdraws money asynchronously.
     *
     * @param uuid The player UUID
     * @param amount The amount to withdraw
     * @return An async result with the response
     */
    public abstract AsyncResult<EconomyResponse> withdrawAsync(UUID uuid, BigDecimal amount);

    /**
     * Withdraws money asynchronously (legacy String-based, pre-UUID servers).
     *
     * @param playerName The player name
     * @param amount The amount to withdraw
     * @return An async result with the response
     * @deprecated Use UUID-based method instead
     */
    @Deprecated
    public AsyncResult<EconomyResponse> withdrawAsync(String playerName, BigDecimal amount) {
        throw new UnsupportedOperationException("String-based player identification not supported");
    }

    /**
     * Checks if an account exists asynchronously.
     *
     * @param uuid The player UUID
     * @return An async result with the account existence status
     */
    public abstract AsyncResult<Boolean> hasAccountAsync(UUID uuid);

    /**
     * Checks if an account exists asynchronously (legacy String-based, pre-UUID servers).
     *
     * @param playerName The player name
     * @return An async result with the account existence status
     * @deprecated Use UUID-based method instead
     */
    @Deprecated
    public AsyncResult<Boolean> hasAccountAsync(String playerName) {
        throw new UnsupportedOperationException("String-based player identification not supported");
    }

    /**
     * Gets the currency symbol/name (singular).
     *
     * @return Currency singular name (e.g., "Dollar" or "$")
     */
    public abstract String getCurrency();

    /**
     * Gets the currency symbol/name (plural).
     *
     * @return Currency plural name (e.g., "Dollars" or "$")
     */
    public String getCurrencyPlural() {
        return getCurrency() + "s";
    }

    /**
     * Formats an amount with currency symbol.
     *
     * @param amount The amount to format
     * @return Formatted string (e.g., "$100.00")
     */
    public String format(BigDecimal amount) {
        return getCurrency() + amount.toPlainString();
    }
}
