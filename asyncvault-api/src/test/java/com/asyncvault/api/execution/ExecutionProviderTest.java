package com.asyncvault.api.execution;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.SyncResult;
import com.asyncvault.api.TestExecutionProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ExecutionProviderTest {

    private ExecutionProvider provider;

    @Before
    public void setUp() {
        provider = TestExecutionProvider.INSTANCE;
        ExecutionProviderContext.set(provider);
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    @Test
    public void testSyncExecutorNotNull() {
        assertNotNull(provider.syncExecutor());
    }

    @Test
    public void testAsyncExecutorNotNull() {
        assertNotNull(provider.asyncExecutor());
    }

    @Test
    public void testRunSync() throws Exception {
        boolean[] ran = {false};
        provider.runSync(() -> ran[0] = true);
        Thread.sleep(100);
        assertTrue(ran[0]);
    }

    @Test
    public void testRunAsync() throws Exception {
        boolean[] ran = {false};
        provider.runAsync(() -> ran[0] = true);
        Thread.sleep(100);
        assertTrue(ran[0]);
    }

    @Test
    public void testSupplyAsync() throws Exception {
        AsyncResult<String> result = provider.supplyAsync(() -> "hello");
        assertEquals("hello", result.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testSupplySync() throws Exception {
        SyncResult<Integer> result = provider.supplySync(() -> 42);
        assertEquals(Integer.valueOf(42), result.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testSupplyAsyncException() throws Exception {
        AsyncResult<String> result = provider.supplyAsync(() -> {
            throw new RuntimeException("boom");
        });
        try {
            result.get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("boom"));
        }
    }

    @Test
    public void testSupplySyncException() throws Exception {
        SyncResult<String> result = provider.supplySync(() -> {
            throw new RuntimeException("sync boom");
        });
        try {
            result.get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("sync boom"));
        }
    }
}
