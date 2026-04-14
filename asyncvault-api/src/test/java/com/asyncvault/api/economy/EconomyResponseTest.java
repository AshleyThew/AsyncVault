package com.asyncvault.api.economy;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class EconomyResponseTest {

    // --- Factory methods ---

    @Test
    public void testSuccess() {
        EconomyResponse r = EconomyResponse.success(new BigDecimal("100.50"));
        assertEquals(EconomyResponse.ResponseStatus.SUCCESS, r.getStatus());
        assertEquals(new BigDecimal("100.50"), r.getAmount());
        assertNull(r.getErrorMessage());
        assertEquals(0L, r.getTransactionId());
        assertTrue(r.isSuccessful());
        assertFalse(r.isPartial());
        assertFalse(r.requiresRestore());
        assertEquals(EconomyResponse.RestoreAction.NONE, r.getRestoreAction());
        assertEquals(BigDecimal.ZERO, r.getRestoreAmount());
    }

    @Test
    public void testSuccessWithTransactionId() {
        EconomyResponse r = EconomyResponse.success(new BigDecimal("50"), 12345L);
        assertEquals(EconomyResponse.ResponseStatus.SUCCESS, r.getStatus());
        assertEquals(new BigDecimal("50"), r.getAmount());
        assertEquals(12345L, r.getTransactionId());
        assertTrue(r.isSuccessful());
    }

    @Test
    public void testFailure() {
        EconomyResponse r = EconomyResponse.failure("something broke");
        assertEquals(EconomyResponse.ResponseStatus.FAILURE, r.getStatus());
        assertEquals(BigDecimal.ZERO, r.getAmount());
        assertEquals("something broke", r.getErrorMessage());
        assertFalse(r.isSuccessful());
        assertFalse(r.isPartial());
    }

    @Test
    public void testNotImplemented() {
        EconomyResponse r = EconomyResponse.notImplemented();
        assertEquals(EconomyResponse.ResponseStatus.NOT_IMPLEMENTED, r.getStatus());
        assertEquals("Operation not implemented", r.getErrorMessage());
        assertFalse(r.isSuccessful());
    }

    @Test
    public void testAccountNotFound() {
        EconomyResponse r = EconomyResponse.accountNotFound();
        assertEquals(EconomyResponse.ResponseStatus.ACCOUNT_NOT_FOUND, r.getStatus());
        assertEquals("Account not found", r.getErrorMessage());
        assertFalse(r.isSuccessful());
    }

    @Test
    public void testInsufficientFunds() {
        EconomyResponse r = EconomyResponse.insufficientFunds(new BigDecimal("10"), new BigDecimal("50"));
        assertEquals(EconomyResponse.ResponseStatus.INSUFFICIENT_FUNDS, r.getStatus());
        assertEquals(new BigDecimal("10"), r.getAmount());
        assertTrue(r.getErrorMessage().contains("10"));
        assertTrue(r.getErrorMessage().contains("50"));
        assertFalse(r.isSuccessful());
    }

    @Test
    public void testPartial() {
        EconomyResponse r = EconomyResponse.partial(
            new BigDecimal("30"), "partial deposit",
            EconomyResponse.RestoreAction.WITHDRAW, new BigDecimal("30")
        );
        assertEquals(EconomyResponse.ResponseStatus.PARTIAL, r.getStatus());
        assertEquals(new BigDecimal("30"), r.getAmount());
        assertEquals("partial deposit", r.getErrorMessage());
        assertEquals(EconomyResponse.RestoreAction.WITHDRAW, r.getRestoreAction());
        assertEquals(new BigDecimal("30"), r.getRestoreAmount());
        assertTrue(r.isPartial());
        assertTrue(r.requiresRestore());
        assertFalse(r.isSuccessful());
    }

    @Test
    public void testPartialWithTransactionId() {
        EconomyResponse r = EconomyResponse.partial(
            new BigDecimal("20"), "partial withdraw",
            EconomyResponse.RestoreAction.DEPOSIT, new BigDecimal("20"), 999L
        );
        assertEquals(EconomyResponse.ResponseStatus.PARTIAL, r.getStatus());
        assertEquals(999L, r.getTransactionId());
        assertTrue(r.isPartial());
        assertTrue(r.requiresRestore());
    }

    @Test
    public void testPartialNoRestoreNeeded() {
        EconomyResponse r = EconomyResponse.partial(
            new BigDecimal("10"), "partial but no restore",
            EconomyResponse.RestoreAction.NONE, BigDecimal.ZERO
        );
        assertTrue(r.isPartial());
        assertFalse(r.requiresRestore());
    }

    @Test
    public void testPartialZeroRestoreAmount() {
        EconomyResponse r = EconomyResponse.partial(
            new BigDecimal("10"), "partial zero amount",
            EconomyResponse.RestoreAction.DEPOSIT, BigDecimal.ZERO
        );
        assertTrue(r.isPartial());
        assertFalse(r.requiresRestore());
    }

    // --- Constructor ---

    @Test
    public void testFourArgConstructor() {
        EconomyResponse r = new EconomyResponse(
            EconomyResponse.ResponseStatus.SUCCESS,
            new BigDecimal("100"), null, 42L
        );
        assertEquals(EconomyResponse.ResponseStatus.SUCCESS, r.getStatus());
        assertEquals(new BigDecimal("100"), r.getAmount());
        assertNull(r.getErrorMessage());
        assertEquals(42L, r.getTransactionId());
        assertEquals(EconomyResponse.RestoreAction.NONE, r.getRestoreAction());
        assertEquals(BigDecimal.ZERO, r.getRestoreAmount());
    }

    @Test
    public void testSixArgConstructor() {
        EconomyResponse r = new EconomyResponse(
            EconomyResponse.ResponseStatus.PARTIAL,
            new BigDecimal("50"), "partial msg", 100L,
            EconomyResponse.RestoreAction.DEPOSIT, new BigDecimal("25")
        );
        assertEquals(EconomyResponse.ResponseStatus.PARTIAL, r.getStatus());
        assertEquals(new BigDecimal("50"), r.getAmount());
        assertEquals("partial msg", r.getErrorMessage());
        assertEquals(100L, r.getTransactionId());
        assertEquals(EconomyResponse.RestoreAction.DEPOSIT, r.getRestoreAction());
        assertEquals(new BigDecimal("25"), r.getRestoreAmount());
    }

    // --- toString ---

    @Test
    public void testToString() {
        EconomyResponse r = EconomyResponse.success(new BigDecimal("10"));
        String s = r.toString();
        assertTrue(s.contains("SUCCESS"));
        assertTrue(s.contains("10"));
        assertTrue(s.contains("EconomyResponse"));
    }

    @Test
    public void testToStringWithError() {
        EconomyResponse r = EconomyResponse.failure("oops");
        String s = r.toString();
        assertTrue(s.contains("FAILURE"));
        assertTrue(s.contains("oops"));
    }

}
