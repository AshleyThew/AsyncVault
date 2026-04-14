package com.asyncvault.api.economy;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.TestExecutionProvider;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class BankAccountEconomyProviderTest {

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    @Test
    public void testGetBankAccountId() {
        TestBankProvider p = new TestBankProvider("bank-001");
        assertEquals("bank-001", p.getBankAccountId());
    }

    @Test(expected = NullPointerException.class)
    public void testNullBankAccountId() {
        new TestBankProvider(null);
    }

    @Test
    public void testConstructorWithExecutionProvider() {
        TestBankProvider p = new TestBankProvider(TestExecutionProvider.INSTANCE, "bank-002");
        assertEquals("bank-002", p.getBankAccountId());
        assertSame(TestExecutionProvider.INSTANCE, p.getExecutionProvider());
    }

    @Test(expected = NullPointerException.class)
    public void testNullBankAccountIdWithExecutionProvider() {
        new TestBankProvider(TestExecutionProvider.INSTANCE, null);
    }

    @Test
    public void testInheritsEconomyProvider() {
        TestBankProvider p = new TestBankProvider("bank-003");
        assertTrue(p instanceof EconomyProvider);
        assertEquals("BankTestEco", p.getName());
    }

    private static class TestBankProvider extends BankAccountEconomyProvider {
        TestBankProvider(String bankAccountId) {
            super(bankAccountId);
        }

        TestBankProvider(ExecutionProvider ep, String bankAccountId) {
            super(ep, bankAccountId);
        }

        @Override
        public String getName() { return "BankTestEco"; }

        @Override
        public boolean supportsAsyncOperations() { return true; }

        @Override
        public AsyncResult<BigDecimal> getBalanceAsync(UUID uuid) {
            return AsyncResult.completed(getExecutionProvider(), new BigDecimal("5000"));
        }

        @Override
        public AsyncResult<EconomyResponse> depositAsync(UUID uuid, BigDecimal amount) {
            return AsyncResult.completed(getExecutionProvider(), EconomyResponse.success(amount));
        }

        @Override
        public AsyncResult<EconomyResponse> withdrawAsync(UUID uuid, BigDecimal amount) {
            return AsyncResult.completed(getExecutionProvider(), EconomyResponse.success(amount));
        }

        @Override
        public AsyncResult<EconomyResponse> restoreAsync(UUID uuid, EconomyResponse partial) {
            return AsyncResult.completed(getExecutionProvider(), EconomyResponse.success(partial.getAmount()));
        }

        @Override
        public AsyncResult<Boolean> hasAccountAsync(UUID uuid) {
            return AsyncResult.completed(getExecutionProvider(), true);
        }

        @Override
        public String getCurrency() { return "Gold"; }
    }
}
