package com.asyncvault.api.chat;

import com.asyncvault.api.TestExecutionProvider;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class WorldChatProviderTest {

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
        TestWorldChatProvider p = new TestWorldChatProvider("nether");
        assertEquals("nether", p.getWorldName());
    }

    @Test(expected = NullPointerException.class)
    public void testNullWorldName() {
        new TestWorldChatProvider(null);
    }

    @Test
    public void testConstructorWithExecutionProvider() {
        TestWorldChatProvider p = new TestWorldChatProvider(TestExecutionProvider.INSTANCE, "end");
        assertEquals("end", p.getWorldName());
        assertSame(TestExecutionProvider.INSTANCE, p.getExecutionProvider());
    }

    @Test(expected = NullPointerException.class)
    public void testNullWorldNameWithExecutionProvider() {
        new TestWorldChatProvider(TestExecutionProvider.INSTANCE, null);
    }

    @Test
    public void testInheritsChatProvider() {
        TestWorldChatProvider p = new TestWorldChatProvider("overworld");
        assertTrue(p instanceof ChatProvider);
        assertEquals("WorldTestChat", p.getName());
    }

    private static class TestWorldChatProvider extends WorldChatProvider {
        TestWorldChatProvider(String worldName) {
            super(worldName);
        }

        TestWorldChatProvider(ExecutionProvider ep, String worldName) {
            super(ep, worldName);
        }

        @Override
        public String getName() { return "WorldTestChat"; }

        @Override
        public boolean supportsAsyncOperations() { return true; }

        @Override
        public String getPlayerPrefix(UUID uuid) { return "[W]"; }

        @Override
        public String getPlayerSuffix(UUID uuid) { return "[/W]"; }
    }
}
