package com.asyncvault.api.chat;

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

public class ChatProviderTest {

    private TestChatProvider provider;
    private final UUID testUuid = UUID.randomUUID();

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
        provider = new TestChatProvider();
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    // --- Basic getters ---

    @Test
    public void testGetName() {
        assertEquals("TestChat", provider.getName());
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

    // --- Prefix ---

    @Test
    public void testGetPlayerPrefix() {
        assertEquals("[Admin] ", provider.getPlayerPrefix(testUuid));
    }

    @Test
    public void testGetPlayerPrefixWorldDelegatesToDefault() {
        assertEquals("[Admin] ", provider.getPlayerPrefix(testUuid, "world"));
    }

    @Test
    public void testGetPlayerPrefixAsync() throws Exception {
        String prefix = provider.getPlayerPrefixAsync(testUuid).get(2, TimeUnit.SECONDS);
        assertEquals("[Admin] ", prefix);
    }

    // --- Suffix ---

    @Test
    public void testGetPlayerSuffix() {
        assertEquals(" [VIP]", provider.getPlayerSuffix(testUuid));
    }

    @Test
    public void testGetPlayerSuffixWorldDelegatesToDefault() {
        assertEquals(" [VIP]", provider.getPlayerSuffix(testUuid, "world"));
    }

    @Test
    public void testGetPlayerSuffixAsync() throws Exception {
        String suffix = provider.getPlayerSuffixAsync(testUuid).get(2, TimeUnit.SECONDS);
        assertEquals(" [VIP]", suffix);
    }

    // --- Info nodes (default throws) ---

    @Test(expected = UnsupportedOperationException.class)
    public void testGetPlayerInfoStringThrows() {
        provider.getPlayerInfoString(testUuid, "rank");
    }

    @Test
    public void testGetPlayerInfoStringAsyncThrows() throws Exception {
        try {
            provider.getPlayerInfoStringAsync(testUuid, "rank").get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    // --- Group prefix/suffix (default throws) ---

    @Test(expected = UnsupportedOperationException.class)
    public void testGetGroupPrefixThrows() {
        provider.getGroupPrefix("admin");
    }

    @Test
    public void testGetGroupPrefixAsyncThrows() throws Exception {
        try {
            provider.getGroupPrefixAsync("admin").get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetGroupSuffixThrows() {
        provider.getGroupSuffix("vip");
    }

    @Test
    public void testGetGroupSuffixAsyncThrows() throws Exception {
        try {
            provider.getGroupSuffixAsync("vip").get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    // --- Set prefix/suffix (default throws) ---

    @Test(expected = UnsupportedOperationException.class)
    public void testSetPlayerPrefixThrows() {
        provider.setPlayerPrefix(testUuid, "[New]");
    }

    @Test
    public void testSetPlayerPrefixAsyncThrows() throws Exception {
        try {
            provider.setPlayerPrefixAsync(testUuid, "[New]").get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetPlayerSuffixThrows() {
        provider.setPlayerSuffix(testUuid, "[New]");
    }

    @Test
    public void testSetPlayerSuffixAsyncThrows() throws Exception {
        try {
            provider.setPlayerSuffixAsync(testUuid, "[New]").get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    // --- Async success paths for optional methods ---

    @Test
    public void testGetPlayerInfoStringAsyncSuccess() throws Exception {
        FullChatProvider full = new FullChatProvider();
        String result = full.getPlayerInfoStringAsync(testUuid, "rank").get(2, TimeUnit.SECONDS);
        assertEquals("elite", result);
    }

    @Test
    public void testGetGroupPrefixAsyncSuccess() throws Exception {
        FullChatProvider full = new FullChatProvider();
        String result = full.getGroupPrefixAsync("admin").get(2, TimeUnit.SECONDS);
        assertEquals("[A]", result);
    }

    @Test
    public void testGetGroupSuffixAsyncSuccess() throws Exception {
        FullChatProvider full = new FullChatProvider();
        String result = full.getGroupSuffixAsync("vip").get(2, TimeUnit.SECONDS);
        assertEquals("[V]", result);
    }

    @Test
    public void testSetPlayerPrefixAsyncSuccess() throws Exception {
        FullChatProvider full = new FullChatProvider();
        boolean result = full.setPlayerPrefixAsync(testUuid, "[New]").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    public void testSetPlayerSuffixAsyncSuccess() throws Exception {
        FullChatProvider full = new FullChatProvider();
        boolean result = full.setPlayerSuffixAsync(testUuid, "[S]").get(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    // --- Constructor with explicit ExecutionProvider ---

    @Test
    public void testConstructorWithExecutionProvider() {
        TestChatProvider p = new TestChatProvider(TestExecutionProvider.INSTANCE);
        assertSame(TestExecutionProvider.INSTANCE, p.getExecutionProvider());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullExecutionProvider() {
        new TestChatProvider(null);
    }

    // --- Concrete test implementations ---

    private static class TestChatProvider extends ChatProvider {

        TestChatProvider() {
            super();
        }

        TestChatProvider(ExecutionProvider ep) {
            super(ep);
        }

        @Override
        public String getName() {
            return "TestChat";
        }

        @Override
        public boolean supportsAsyncOperations() {
            return true;
        }

        @Override
        public String getPlayerPrefix(UUID uuid) {
            return "[Admin] ";
        }

        @Override
        public String getPlayerSuffix(UUID uuid) {
            return " [VIP]";
        }
    }

    private static class FullChatProvider extends TestChatProvider {
        @Override
        public String getPlayerInfoString(UUID uuid, String node) {
            return "elite";
        }

        @Override
        public String getGroupPrefix(String groupName) {
            return "[A]";
        }

        @Override
        public String getGroupSuffix(String groupName) {
            return "[V]";
        }

        @Override
        public boolean setPlayerPrefix(UUID uuid, String prefix) {
            return true;
        }

        @Override
        public boolean setPlayerSuffix(UUID uuid, String suffix) {
            return true;
        }
    }
}
