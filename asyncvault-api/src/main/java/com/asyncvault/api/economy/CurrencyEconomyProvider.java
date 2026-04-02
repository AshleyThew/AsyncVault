package com.asyncvault.api.economy;

import com.asyncvault.api.execution.ExecutionProvider;

import java.util.Objects;

/**
 * Base economy provider variant bound to a specific currency.
 */
public abstract class CurrencyEconomyProvider extends EconomyProvider {

    private final String currencyId;

    protected CurrencyEconomyProvider(String currencyId) {
        super();
        this.currencyId = Objects.requireNonNull(currencyId, "currencyId");
    }

    protected CurrencyEconomyProvider(ExecutionProvider executionProvider, String currencyId) {
        super(executionProvider);
        this.currencyId = Objects.requireNonNull(currencyId, "currencyId");
    }

    /**
     * @return currency identifier this provider instance is scoped to
     */
    public String getCurrencyId() {
        return currencyId;
    }
}
