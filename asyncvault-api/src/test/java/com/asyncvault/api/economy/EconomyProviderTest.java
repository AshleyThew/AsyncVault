package com.asyncvault.api.economy;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.TestExecutionProvider;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class EconomyProviderTest {

    private TestEconomyProvider provider;
    private final UUID testUuid = UUID.randomUUID();

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
        provider = new TestEconomyProvider();
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    // --- Basic getters ---

    @Test
    public void testGetName() {
        assertEquals("TestEconomy", provider.getName());
    }

    @Test
    public void testSupportsAsyncOperations() {
        assertTrue(provider.supportsAsyncOperations());
    }

    @Test
    public void testGetExecutionProvider() {
        assertSame(TestExecutionProvider.INSTANCE, provider.getExecutionProvider());
    }

    // --- World scoping defaults ---

    @Test
    public void testSupportsWorldScopingDefault() {
        assertFalse(provider.supportsWorldScoping());
    }

    @Test
    public void testGetWorldScopedProviderDefault() {
        assertSame(provider, provider.getWorldScopedProvider());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetWorldScopedProviderWithNameThrows() {
        provider.getWorldScopedProvider("world");
    }

    // --- Bank account defaults ---

    @Test
    public void testSupportsBankAccountsDefault() {
        assertFalse(provider.supportsBankAccounts());
    }

    @Test
    public void testGetBankAccountProviderDefault() {
        assertSame(provider, provider.getBankAccountProvider());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetBankAccountProviderWithIdThrows() {
        provider.getBankAccountProvider("bank123");
    }

    // --- Currency defaults ---

    @Test
    public void testSupportsMultipleCurrenciesDefault() {
        assertFalse(provider.supportsMultipleCurrencies());
    }

    @Test
    public void testGetSupportedCurrencies() {
        List<String> currencies = provider.getSupportedCurrencies();
        assertEquals(1, currencies.size());
        assertEquals("Dollar", currencies.get(0));
    }

    @Test
    public void testGetCurrencyProviderKnown() {
        assertSame(provider, provider.getCurrencyProvider("Dollar"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetCurrencyProviderUnknownThrows() {
        provider.getCurrencyProvider("Euro");
    }

    @Test
    public void testGetCurrencyProviders() {
        List<EconomyProvider> providers = provider.getCurrencyProviders();
        assertEquals(1, providers.size());
        assertSame(provider, providers.get(0));
    }

    // --- Balance ---

    @Test
    public void testGetBalanceAsyncWorldDelegatesToDefault() throws Exception {
        BigDecimal balance = provider.getBalanceAsync(testUuid, "world_nether").get(2, java.util.concurrent.TimeUnit.SECONDS);
        assertEquals(new BigDecimal("1000"), balance);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetBalanceAsyncStringThrows() {
        provider.getBalanceAsync("playerName");
    }

    // --- Deposit ---


    // --- Restore ---

    @Test(expected = UnsupportedOperationException.class)
    public void testRestoreAsyncStringThrows() {
        provider.restoreAsync("playerName", EconomyResponse.success(BigDecimal.TEN));
    }

    // --- HasAccount ---

    @Test(expected = UnsupportedOperationException.class)
    public void testHasAccountAsyncStringThrows() {
        provider.hasAccountAsync("playerName");
    }

    // --- Currency formatting ---

    @Test
    public void testGetCurrency() {
        assertEquals("Dollar", provider.getCurrency());
    }

    @Test
    public void testGetCurrencyPlural() {
        assertEquals("Dollars", provider.getCurrencyPlural());
    }

    @Test
    public void testFormat() {
        String formatted = provider.format(new BigDecimal("100.50"));
        assertEquals("Dollar100.50", formatted);
    }

    @Test
    public void testFormatZero() {
        assertEquals("Dollar0", provider.format(BigDecimal.ZERO));
    }

    // --- Constructor with explicit ExecutionProvider ---

    @Test
    public void testConstructorWithExecutionProvider() {
        ExecutionProvider ep = TestExecutionProvider.INSTANCE;
        EconomyProvider p = new TestEconomyProvider(ep);
        assertSame(ep, p.getExecutionProvider());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullExecutionProvider() {
        new TestEconomyProvider(null);
    }

    // --- Concrete test implementation ---

    private static class TestEconomyProvider extends EconomyProvider {

        TestEconomyProvider() {
            super();
        }

        TestEconomyProvider(ExecutionProvider ep) {
            super(ep);
        }

        @Override
        public String getName() {
            return "TestEconomy";
        }

        @Override
        public boolean supportsAsyncOperations() {
            return true;
        }

        @Override
        public AsyncResult<BigDecimal> getBalanceAsync(UUID uuid) {
            return AsyncResult.completed(getExecutionProvider(), new BigDecimal("1000"));
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
        public AsyncResult<EconomyResponse> restoreAsync(UUID uuid, EconomyResponse partialResponse) {
            return AsyncResult.completed(getExecutionProvider(), EconomyResponse.success(partialResponse.getAmount()));
        }

        @Override
        public AsyncResult<Boolean> hasAccountAsync(UUID uuid) {
            return AsyncResult.completed(getExecutionProvider(), true);
        }

        @Override
        public String getCurrency() {
            return "Dollar";
        }
    }
}
