package com.asyncvault.api.economy;

import com.asyncvault.api.execution.ExecutionProvider;

import java.util.Objects;

/**
 * Base economy provider variant bound to a specific bank account scope.
 */
public abstract class BankAccountEconomyProvider extends EconomyProvider {

    private final String bankAccountId;

    protected BankAccountEconomyProvider(String bankAccountId) {
        super();
        this.bankAccountId = Objects.requireNonNull(bankAccountId, "bankAccountId");
    }

    protected BankAccountEconomyProvider(ExecutionProvider executionProvider, String bankAccountId) {
        super(executionProvider);
        this.bankAccountId = Objects.requireNonNull(bankAccountId, "bankAccountId");
    }

    /**
     * @return bank account id this provider instance is scoped to
     */
    public String getBankAccountId() {
        return bankAccountId;
    }
}
