package com.asyncvault.api.service;

import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.permission.PermissionProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class AsyncVaultServiceRegistryTest {

    private AsyncVaultServiceRegistry registry;

    @Before
    public void setUp() {
        registry = AsyncVaultServiceRegistry.getInstance();
        registry.clear();
    }

    @After
    public void tearDown() {
        registry.clear();
    }

    // --- Singleton ---

    @Test
    public void testGetInstanceReturnsSameInstance() {
        assertSame(AsyncVaultServiceRegistry.getInstance(), AsyncVaultServiceRegistry.getInstance());
    }

    // --- Register / getProvider ---

    @Test
    public void testRegisterAndGet() {
        String service = "hello";
        registry.register(String.class, service);
        Optional<String> result = registry.getProvider(String.class);
        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
    }

    @Test
    public void testGetProviderNotRegistered() {
        Optional<String> result = registry.getProvider(String.class);
        assertFalse(result.isPresent());
    }

    @Test
    public void testRegisterOverwrites() {
        registry.register(String.class, "first");
        registry.register(String.class, "second");
        assertEquals("second", registry.getProvider(String.class).get());
    }

    @Test(expected = NullPointerException.class)
    public void testRegisterNullClass() {
        registry.register(null, "value");
    }

    @Test(expected = NullPointerException.class)
    public void testRegisterNullInstance() {
        registry.register(String.class, null);
    }

    // --- Unregister ---

    @Test
    public void testUnregister() {
        registry.register(String.class, "hello");
        assertTrue(registry.isRegistered(String.class));
        registry.unregister(String.class);
        assertFalse(registry.isRegistered(String.class));
        assertFalse(registry.getProvider(String.class).isPresent());
    }

    @Test
    public void testUnregisterNotRegistered() {
        // Should not throw
        registry.unregister(String.class);
    }

    @Test(expected = NullPointerException.class)
    public void testUnregisterNullClass() {
        registry.unregister(null);
    }

    // --- isRegistered ---

    @Test
    public void testIsRegistered() {
        assertFalse(registry.isRegistered(String.class));
        registry.register(String.class, "val");
        assertTrue(registry.isRegistered(String.class));
    }

    @Test(expected = NullPointerException.class)
    public void testIsRegisteredNullClass() {
        registry.isRegistered(null);
    }

    // --- Clear ---

    @Test
    public void testClear() {
        registry.register(String.class, "a");
        registry.register(Integer.class, 1);
        assertTrue(registry.isRegistered(String.class));
        assertTrue(registry.isRegistered(Integer.class));
        registry.clear();
        assertFalse(registry.isRegistered(String.class));
        assertFalse(registry.isRegistered(Integer.class));
    }

    // --- Type safety ---

    @Test
    public void testMultipleDifferentTypes() {
        registry.register(String.class, "str");
        registry.register(Integer.class, 42);
        registry.register(Double.class, 3.14);

        assertEquals("str", registry.getProvider(String.class).get());
        assertEquals(Integer.valueOf(42), registry.getProvider(Integer.class).get());
        assertEquals(Double.valueOf(3.14), registry.getProvider(Double.class).get());
    }

    @Test(expected = NullPointerException.class)
    public void testGetProviderNullClass() {
        registry.getProvider(null);
    }

    // --- Thread safety basic check ---

    @Test
    public void testConcurrentRegisterAndGet() throws Exception {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                registry.register(String.class, "thread-" + idx);
                registry.getProvider(String.class);
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(2000);

        // After all threads, exactly one value registered
        assertTrue(registry.getProvider(String.class).isPresent());
    }
}
