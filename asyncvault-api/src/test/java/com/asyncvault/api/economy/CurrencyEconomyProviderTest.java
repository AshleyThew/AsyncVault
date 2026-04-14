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

public class CurrencyEconomyProviderTest {

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    @Test
    public void testGetCurrencyId() {
        TestCurrencyProvider p = new TestCurrencyProvider("EUR");
        assertEquals("EUR", p.getCurrencyId());
    }

    @Test(expected = NullPointerException.class)
    public void testNullCurrencyId() {
        new TestCurrencyProvider(null);
    }

    @Test
    public void testConstructorWithExecutionProvider() {
        TestCurrencyProvider p = new TestCurrencyProvider(TestExecutionProvider.INSTANCE, "GBP");
        assertEquals("GBP", p.getCurrencyId());
        assertSame(TestExecutionProvider.INSTANCE, p.getExecutionProvider());
    }

    @Test(expected = NullPointerException.class)
    public void testNullCurrencyIdWithExecutionProvider() {
        new TestCurrencyProvider(TestExecutionProvider.INSTANCE, null);
    }

    @Test
    public void testInheritsEconomyProvider() {
        TestCurrencyProvider p = new TestCurrencyProvider("JPY");
        assertTrue(p instanceof EconomyProvider);
        assertEquals("CurrencyTestEco", p.getName());
    }

    private static class TestCurrencyProvider extends CurrencyEconomyProvider {
        TestCurrencyProvider(String currencyId) {
            super(currencyId);
        }

        TestCurrencyProvider(ExecutionProvider ep, String currencyId) {
            super(ep, currencyId);
        }

        @Override
        public String getName() { return "CurrencyTestEco"; }

        @Override
        public boolean supportsAsyncOperations() { return true; }

        @Override
        public AsyncResult<BigDecimal> getBalanceAsync(UUID uuid) {
            return AsyncResult.completed(getExecutionProvider(), new BigDecimal("2000"));
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
        public String getCurrency() { return "Coin"; }
    }
}
