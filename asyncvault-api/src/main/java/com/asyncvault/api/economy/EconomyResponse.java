package com.asyncvault.api.economy;

import java.math.BigDecimal;

/**
 * Response object for economy operations.
 * Follows VaultAPI's pattern: use responses instead of exceptions for expected failures.
 */
public class EconomyResponse {

    public enum RestoreAction {
        /** No restore action is required */
        NONE,
        /** Restore by depositing back into the account */
        DEPOSIT,
        /** Restore by withdrawing from the account */
        WITHDRAW
    }
    
    public enum ResponseStatus {
        /** Operation succeeded */
        SUCCESS,
        /** Operation partially applied; caller should invoke restoreAsync */
        PARTIAL,
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
    private final RestoreAction restoreAction;
    private final BigDecimal restoreAmount;

    /**
     * Creates a response.
     *
     * @param status The response status
     * @param amount The amount affected (if applicable)
     * @param errorMessage Error description (null if successful)
     * @param transactionId Optional transaction identifier
     */
    public EconomyResponse(ResponseStatus status, BigDecimal amount, String errorMessage, long transactionId) {
        this(status, amount, errorMessage, transactionId, RestoreAction.NONE, BigDecimal.ZERO);
    }

    /**
     * Creates a response.
     *
     * @param status The response status
     * @param amount The amount affected (if applicable)
     * @param errorMessage Error description (null if successful)
     * @param transactionId Optional transaction identifier
     * @param restoreAction Restore action required to compensate a partial transaction
     * @param restoreAmount Restore amount required for compensation
     */
    public EconomyResponse(
        ResponseStatus status,
        BigDecimal amount,
        String errorMessage,
        long transactionId,
        RestoreAction restoreAction,
        BigDecimal restoreAmount
    ) {
        this.status = status;
        this.amount = amount;
        this.errorMessage = errorMessage;
        this.transactionId = transactionId;
        this.restoreAction = restoreAction;
        this.restoreAmount = restoreAmount;
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

    public RestoreAction getRestoreAction() {
        return restoreAction;
    }

    public BigDecimal getRestoreAmount() {
        return restoreAmount;
    }

    public boolean isSuccessful() {
        return status == ResponseStatus.SUCCESS;
    }

    public boolean isPartial() {
        return status == ResponseStatus.PARTIAL;
    }

    public boolean requiresRestore() {
        return isPartial() && restoreAction != RestoreAction.NONE && restoreAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return String.format("EconomyResponse{status=%s, amount=%s, error=%s, restoreAction=%s, restoreAmount=%s}",
            status, amount, errorMessage, restoreAction, restoreAmount);
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

    public static EconomyResponse partial(
        BigDecimal amount,
        String message,
        RestoreAction restoreAction,
        BigDecimal restoreAmount
    ) {
        return new EconomyResponse(ResponseStatus.PARTIAL, amount, message, 0, restoreAction, restoreAmount);
    }

    public static EconomyResponse partial(
        BigDecimal amount,
        String message,
        RestoreAction restoreAction,
        BigDecimal restoreAmount,
        long transactionId
    ) {
        return new EconomyResponse(ResponseStatus.PARTIAL, amount, message, transactionId, restoreAction, restoreAmount);
    }
}
