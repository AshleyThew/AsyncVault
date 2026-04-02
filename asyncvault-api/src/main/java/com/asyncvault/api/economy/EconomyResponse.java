package com.asyncvault.api.economy;

import java.math.BigDecimal;

/**
 * Response object for economy operations.
 * Follows VaultAPI's pattern: use responses instead of exceptions for expected failures.
 */
public class EconomyResponse {
    
    public enum ResponseStatus {
        /** Operation succeeded */
        SUCCESS,
        /** Generic failure */
        FAILURE,
        /** Operation not implemented by this provider */
        NOT_IMPLEMENTED,
        /** Account not found */
        ACCOUNT_NOT_FOUND,
        /** Insufficient funds */
        INSUFFICIENT_FUNDS
    }

    private final ResponseStatus status;
    private final BigDecimal amount;
    private final String errorMessage;
    private final long transactionId;

    /**
     * Creates a response.
     *
     * @param status The response status
     * @param amount The amount affected (if applicable)
     * @param errorMessage Error description (null if successful)
     * @param transactionId Optional transaction identifier
     */
    public EconomyResponse(ResponseStatus status, BigDecimal amount, String errorMessage, long transactionId) {
        this.status = status;
        this.amount = amount;
        this.errorMessage = errorMessage;
        this.transactionId = transactionId;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public boolean isSuccessful() {
        return status == ResponseStatus.SUCCESS;
    }

    @Override
    public String toString() {
        return String.format("EconomyResponse{status=%s, amount=%s, error=%s}",
            status, amount, errorMessage);
    }

    // Factory methods
    public static EconomyResponse success(BigDecimal amount) {
        return new EconomyResponse(ResponseStatus.SUCCESS, amount, null, 0);
    }

    public static EconomyResponse success(BigDecimal amount, long transactionId) {
        return new EconomyResponse(ResponseStatus.SUCCESS, amount, null, transactionId);
    }

    public static EconomyResponse failure(String message) {
        return new EconomyResponse(ResponseStatus.FAILURE, BigDecimal.ZERO, message, 0);
    }

    public static EconomyResponse notImplemented() {
        return new EconomyResponse(ResponseStatus.NOT_IMPLEMENTED, BigDecimal.ZERO, "Operation not implemented", 0);
    }

    public static EconomyResponse accountNotFound() {
        return new EconomyResponse(ResponseStatus.ACCOUNT_NOT_FOUND, BigDecimal.ZERO, "Account not found", 0);
    }

    public static EconomyResponse insufficientFunds(BigDecimal available, BigDecimal required) {
        return new EconomyResponse(ResponseStatus.INSUFFICIENT_FUNDS, available, 
            String.format("Insufficient funds: have %s, need %s", available, required), 0);
    }
}
