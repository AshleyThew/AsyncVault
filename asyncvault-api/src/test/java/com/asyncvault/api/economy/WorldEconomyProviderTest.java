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

public class WorldEconomyProviderTest {

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    @Test
    public void testGetWorldName() {
        TestWorldEconomyProvider p = new TestWorldEconomyProvider("world_nether");
        assertEquals("world_nether", p.getWorldName());
    }

    @Test(expected = NullPointerException.class)
    public void testNullWorldName() {
        new TestWorldEconomyProvider(null);
    }

    @Test
    public void testConstructorWithExecutionProvider() {
        TestWorldEconomyProvider p = new TestWorldEconomyProvider(TestExecutionProvider.INSTANCE, "overworld");
        assertEquals("overworld", p.getWorldName());
        assertSame(TestExecutionProvider.INSTANCE, p.getExecutionProvider());
    }

    @Test(expected = NullPointerException.class)
    public void testNullWorldNameWithExecutionProvider() {
        new TestWorldEconomyProvider(TestExecutionProvider.INSTANCE, null);
    }

    @Test
    public void testInheritsEconomyProvider() {
        TestWorldEconomyProvider p = new TestWorldEconomyProvider("end");
        assertTrue(p instanceof EconomyProvider);
        assertEquals("WorldTestEco", p.getName());
    }

    private static class TestWorldEconomyProvider extends WorldEconomyProvider {
        TestWorldEconomyProvider(String worldName) {
            super(worldName);
        }

        TestWorldEconomyProvider(ExecutionProvider ep, String worldName) {
            super(ep, worldName);
        }

        @Override
        public String getName() { return "WorldTestEco"; }

        @Override
        public boolean supportsAsyncOperations() { return true; }

        @Override
        public AsyncResult<BigDecimal> getBalanceAsync(UUID uuid) {
            return AsyncResult.completed(getExecutionProvider(), BigDecimal.TEN);
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
        public String getCurrency() { return "Gem"; }
    }
}
