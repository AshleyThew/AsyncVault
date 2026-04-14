package com.asyncvault.api.execution;

import com.asyncvault.api.TestExecutionProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExecutionProviderContextTest {

    @Before
    public void setUp() {
        ExecutionProviderContext.clear();
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    @Test
    public void testSetAndGet() {
        assertNull(ExecutionProviderContext.get());
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
        assertSame(TestExecutionProvider.INSTANCE, ExecutionProviderContext.get());
    }

    @Test(expected = NullPointerException.class)
    public void testSetNull() {
        ExecutionProviderContext.set(null);
    }

    @Test
    public void testClear() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
        assertNotNull(ExecutionProviderContext.get());
        ExecutionProviderContext.clear();
        assertNull(ExecutionProviderContext.get());
    }

    @Test
    public void testRequireWhenSet() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
        ExecutionProvider result = ExecutionProviderContext.require();
        assertSame(TestExecutionProvider.INSTANCE, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireWhenNotSet() {
        ExecutionProviderContext.require();
    }

    @Test
    public void testOverwrite() {
        ExecutionProvider first = TestExecutionProvider.INSTANCE;
        ExecutionProvider second = new TestExecutionProvider();
        ExecutionProviderContext.set(first);
        assertSame(first, ExecutionProviderContext.get());
        ExecutionProviderContext.set(second);
        assertSame(second, ExecutionProviderContext.get());
    }
}
