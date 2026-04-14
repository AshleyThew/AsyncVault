package com.asyncvault.api.permission;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.TestExecutionProvider;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PermissionProviderTest {

    private TestPermissionProvider provider;
    private final UUID testUuid = UUID.randomUUID();

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
        provider = new TestPermissionProvider();
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    // --- Basic getters ---

    @Test
    public void testGetName() {
        assertEquals("TestPerms", provider.getName());
    }

    @Test
    public void testSupportsAsyncOperations() {
        assertTrue(provider.supportsAsyncOperations());
    }

    @Test
    public void testGetExecutionProvider() {
        assertSame(TestExecutionProvider.INSTANCE, provider.getExecutionProvider());
    }

    // --- Group management default ---

    @Test
    public void testSupportsGroupManagementDefault() {
        assertFalse(provider.supportsGroupManagement());
    }

    // --- Permission checks ---

    @Test
    public void testHasPermission() {
        assertTrue(provider.hasPermission(testUuid, "test.allowed"));
        assertFalse(provider.hasPermission(testUuid, "test.denied"));
    }

    @Test
    public void testHasPermissionWorldDelegatesToDefault() {
        assertTrue(provider.hasPermission(testUuid, "world", "test.allowed"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testHasPermissionStringThrows() {
        provider.hasPermission("playerName", "test.perm");
    }

    @Test
    public void testHasPermissionAsync() throws Exception {
        boolean result = provider.hasPermissionAsync(testUuid, "test.allowed").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    public void testHasPermissionAsyncFalse() throws Exception {
        boolean result = provider.hasPermissionAsync(testUuid, "test.denied").get(2, TimeUnit.SECONDS);
        assertFalse(result);
    }

    @Test
    public void testHasPermissionAsyncWithWorld() throws Exception {
        boolean result = provider.hasPermissionAsync(testUuid, "nether", "test.allowed").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    // --- Grant/Revoke ---

    @Test
    public void testGrantPermission() {
        assertTrue(provider.grantPermission(testUuid, "test.new"));
    }

    @Test
    public void testGrantPermissionAsync() throws Exception {
        boolean result = provider.grantPermissionAsync(testUuid, "test.new").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    public void testRevokePermission() {
        assertTrue(provider.revokePermission(testUuid, "test.old"));
    }

    @Test
    public void testRevokePermissionAsync() throws Exception {
        boolean result = provider.revokePermissionAsync(testUuid, "test.old").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    // --- Group operations (default throws) ---

    @Test(expected = UnsupportedOperationException.class)
    public void testGetPrimaryGroupThrows() {
        provider.getPrimaryGroup(testUuid);
    }

    @Test
    public void testGetPrimaryGroupAsyncThrows() throws Exception {
        try {
            provider.getPrimaryGroupAsync(testUuid).get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddToGroupThrows() {
        provider.addToGroup(testUuid, "admin");
    }

    @Test
    public void testAddToGroupAsyncThrows() throws Exception {
        try {
            provider.addToGroupAsync(testUuid, "admin").get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveFromGroupThrows() {
        provider.removeFromGroup(testUuid, "admin");
    }

    @Test
    public void testRemoveFromGroupAsyncThrows() throws Exception {
        try {
            provider.removeFromGroupAsync(testUuid, "admin").get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    // --- Async success paths for optional methods ---

    @Test
    public void testGetPrimaryGroupAsyncSuccess() throws Exception {
        FullPermissionProvider full = new FullPermissionProvider();
        String result = full.getPrimaryGroupAsync(testUuid).get(2, TimeUnit.SECONDS);
        assertEquals("admin", result);
    }

    @Test
    public void testAddToGroupAsyncSuccess() throws Exception {
        FullPermissionProvider full = new FullPermissionProvider();
        boolean result = full.addToGroupAsync(testUuid, "vip").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    public void testRemoveFromGroupAsyncSuccess() throws Exception {
        FullPermissionProvider full = new FullPermissionProvider();
        boolean result = full.removeFromGroupAsync(testUuid, "vip").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    // --- Constructor ---

    @Test
    public void testConstructorWithExecutionProvider() {
        TestPermissionProvider p = new TestPermissionProvider(TestExecutionProvider.INSTANCE);
        assertSame(TestExecutionProvider.INSTANCE, p.getExecutionProvider());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullExecutionProvider() {
        new TestPermissionProvider(null);
    }

    // --- Concrete test implementation ---

    private static class TestPermissionProvider extends PermissionProvider {

        TestPermissionProvider() {
            super();
        }

        TestPermissionProvider(ExecutionProvider ep) {
            super(ep);
        }

        @Override
        public String getName() {
            return "TestPerms";
        }

        @Override
        public boolean supportsAsyncOperations() {
            return true;
        }

        @Override
        public boolean hasPermission(UUID uuid, String permission) {
            return "test.allowed".equals(permission);
        }

        @Override
        public boolean grantPermission(UUID uuid, String permission) {
            return true;
        }

        @Override
        public boolean revokePermission(UUID uuid, String permission) {
            return true;
        }
    }

    private static class FullPermissionProvider extends TestPermissionProvider {
        @Override
        public String getPrimaryGroup(UUID uuid) {
            return "admin";
        }

        @Override
        public boolean addToGroup(UUID uuid, String groupName) {
            return true;
        }

        @Override
        public boolean removeFromGroup(UUID uuid, String groupName) {
            return true;
        }
    }
}
