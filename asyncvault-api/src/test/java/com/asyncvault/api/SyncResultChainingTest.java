package com.asyncvault.api;

import com.asyncvault.api.execution.ExecutionProviderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Thorough chaining tests for SyncResult covering multi-step chains,
 * error propagation, cross-type conversions, delayed completion,
 * exception-in-chain-function, nested composition, and unwrapThrowable.
 */
public class SyncResultChainingTest {

    private static final long TIMEOUT = 3;

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    // =========================================================================
    // Multi-step chains
    // =========================================================================

    @Test
    public void testMultiStepThenChain() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, 1)
            .then(v -> v + 1)       // 2
            .then(v -> v * 3)       // 6
            .then(v -> v + 10)      // 16
            .then(String::valueOf); // "16"
        assertEquals("16", r.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testThenChainPreservesOrder() throws Exception {
        List<Integer> order = Collections.synchronizedList(new ArrayList<Integer>());
        SyncResult.completed(TestExecutionProvider.INSTANCE, "start")
            .then(v -> { order.add(1); return v; })
            .then(v -> { order.add(2); return v; })
            .then(v -> { order.add(3); return v; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(3, order.size());
        assertEquals(Integer.valueOf(1), order.get(0));
        assertEquals(Integer.valueOf(2), order.get(1));
        assertEquals(Integer.valueOf(3), order.get(2));
    }

    @Test
    public void testThenAcceptAfterThen() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, 5)
            .then(v -> v * 2)
            .then(v -> "result=" + v)
            .thenAccept(captured::set)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("result=10", captured.get());
    }

    @Test
    public void testThenRunAfterMultipleThen() throws Exception {
        AtomicBoolean ran = new AtomicBoolean();
        SyncResult.completed(TestExecutionProvider.INSTANCE, 1)
            .then(v -> v + 1)
            .then(v -> v + 1)
            .thenRun(() -> ran.set(true))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertTrue(ran.get());
    }

    @Test
    public void testThenCombineFollowedByThen() throws Exception {
        SyncResult<Integer> a = SyncResult.completed(TestExecutionProvider.INSTANCE, 10);
        SyncResult<Integer> b = SyncResult.completed(TestExecutionProvider.INSTANCE, 20);
        String result = a.thenCombine(b, Integer::sum)
            .then(v -> v * 2)
            .then(v -> "total=" + v)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("total=60", result);
    }

    @Test
    public void testHandleFollowedByThen() throws Exception {
        String result = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handle((val, ex) -> ex != null ? "recovered" : val)
            .then(v -> v.toUpperCase())
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("RECOVERED", result);
    }

    @Test
    public void testExceptionallyFollowedByThen() throws Exception {
        String result = SyncResult.<Integer>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .exceptionallySync(ex -> 42)
            .then(v -> "val=" + v)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("val=42", result);
    }

    @Test
    public void testWhenCompleteFollowedByThen() throws Exception {
        AtomicReference<String> sideEffect = new AtomicReference<>();
        String result = SyncResult.completed(TestExecutionProvider.INSTANCE, "hello")
            .whenComplete((v, ex) -> sideEffect.set(v))
            .then(v -> v + " world")
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("hello world", result);
        assertEquals("hello", sideEffect.get());
    }

    // =========================================================================
    // Error propagation through chains
    // =========================================================================

    @Test
    public void testErrorPropagatesThroughThen() throws Exception {
        SyncResult<String> r = SyncResult.<Integer>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("original"))
            .then(v -> v + 1)
            .then(String::valueOf);
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("original", e.getCause().getMessage());
        }
    }

    @Test
    public void testErrorPropagatesThroughThenAccept() throws Exception {
        AtomicBoolean reached = new AtomicBoolean();
        SyncResult<Void> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .thenAccept(v -> reached.set(true));
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertFalse(reached.get());
            assertEquals("err", e.getCause().getMessage());
        }
    }

    @Test
    public void testErrorPropagatesThroughThenRun() throws Exception {
        AtomicBoolean reached = new AtomicBoolean();
        SyncResult<Void> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .thenRun(() -> reached.set(true));
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertFalse(reached.get());
        }
    }

    @Test
    public void testErrorPropagatesThroughThenCompose() throws Exception {
        SyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("compose-err"))
            .thenCompose(v -> CompletableFuture.completedFuture(v + "x"));
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("compose-err", e.getCause().getMessage());
        }
    }

    @Test
    public void testErrorPropagatesThroughThenCombine() throws Exception {
        SyncResult<Integer> failed = SyncResult.failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("combine-err"));
        SyncResult<Integer> ok = SyncResult.completed(TestExecutionProvider.INSTANCE, 5);
        SyncResult<Integer> r = failed.thenCombine(ok, Integer::sum);
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("combine-err", e.getCause().getMessage());
        }
    }

    @Test
    public void testErrorRecoveredByHandleMidChain() throws Exception {
        String result = SyncResult.<Integer>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("mid-err"))
            .then(v -> v + 1)
            .handle((val, ex) -> ex != null ? 99 : val)
            .then(v -> "val=" + v)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("val=99", result);
    }

    @Test
    public void testErrorRecoveredByExceptionallyMidChain() throws Exception {
        int result = SyncResult.<Integer>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("x"))
            .then(v -> v * 2)
            .exceptionallySync(ex -> -1)
            .then(v -> v + 100)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(99, result);
    }

    @Test
    public void testWhenCompleteSeesErrorAndPropagates() throws Exception {
        AtomicReference<Throwable> captured = new AtomicReference<>();
        SyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("wc-err"))
            .whenComplete((v, ex) -> captured.set(ex));
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertNotNull(captured.get());
            assertEquals("wc-err", captured.get().getMessage());
        }
    }

    // =========================================================================
    // Exception thrown inside chain function
    // =========================================================================

    @Test
    public void testExceptionInThenFunction() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .then(v -> { throw new RuntimeException("then-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("then-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInThenAsyncFunction() throws Exception {
        AsyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .thenAsync(v -> { throw new RuntimeException("thenAsync-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("thenAsync-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInThenAcceptConsumer() throws Exception {
        SyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .thenAccept(v -> { throw new RuntimeException("accept-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("accept-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInThenRunAction() throws Exception {
        SyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .thenRun(() -> { throw new RuntimeException("run-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("run-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInThenComposeFunction() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .thenCompose(v -> { throw new RuntimeException("compose-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("compose-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInThenCombineFunction() throws Exception {
        SyncResult<Integer> a = SyncResult.completed(TestExecutionProvider.INSTANCE, 1);
        SyncResult<Integer> b = SyncResult.completed(TestExecutionProvider.INSTANCE, 2);
        SyncResult<Integer> r = a.thenCombine(b, (x, y) -> { throw new RuntimeException("combine-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("combine-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInHandleThrows() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .handle((v, ex) -> { throw new RuntimeException("handle-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("handle-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInWhenCompleteAction() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .whenComplete((v, ex) -> { throw new RuntimeException("wc-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("wc-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInExceptionallyHandler() throws Exception {
        SyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("orig"))
            .exceptionallySync(ex -> { throw new RuntimeException("except-boom"); });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("except-boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testExceptionInChainRecoveredByLaterHandle() throws Exception {
        String result = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .then(v -> { throw new RuntimeException("mid-boom"); })
            .then(v -> "should-not-reach")
            .handle((v, ex) -> ex != null ? "caught:" + ex.getMessage() : v)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("caught:mid-boom", result);
    }

    // =========================================================================
    // Cross-type chaining: Sync → Async → Sync
    // =========================================================================

    @Test
    public void testSyncToAsyncToSyncChain() throws Exception {
        String result = SyncResult.completed(TestExecutionProvider.INSTANCE, 10)
            .thenAsync(v -> v * 2)            // AsyncResult<Integer>
            .thenSync(v -> "val=" + v)        // SyncResult<String>
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("val=20", result);
    }

    @Test
    public void testSyncToAsyncViaAsAsync() throws Exception {
        AsyncResult<Integer> async = SyncResult.completed(TestExecutionProvider.INSTANCE, 5).asAsync();
        int result = async.then(v -> v + 10).get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(15, result);
    }

    @Test
    public void testAsyncToSyncViaAsSync() throws Exception {
        SyncResult<Integer> sync = AsyncResult.completed(TestExecutionProvider.INSTANCE, 5).asSync();
        int result = sync.then(v -> v + 10).get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(15, result);
    }

    @Test
    public void testMultipleCrossTypeConversions() throws Exception {
        String result = SyncResult.completed(TestExecutionProvider.INSTANCE, 1)
            .thenAsync(v -> v + 1)          // AsyncResult<Integer> = 2
            .thenSync(v -> v * 3)           // SyncResult<Integer> = 6
            .thenAsync(v -> v + 4)          // AsyncResult<Integer> = 10
            .thenSync(v -> "answer=" + v)   // SyncResult<String>
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("answer=10", result);
    }

    @Test
    public void testSyncThenAcceptAsyncFollowedBySyncRun() throws Exception {
        AtomicReference<String> first = new AtomicReference<>();
        AtomicReference<String> second = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "data")
            .thenAcceptAsync(first::set)   // AsyncResult<Void>
            .thenRunSync(() -> second.set("ran"))  // SyncResult<Void>
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("data", first.get());
        assertEquals("ran", second.get());
    }

    @Test
    public void testCrossTypeErrorPropagation() throws Exception {
        try {
            SyncResult.<Integer>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("cross-err"))
                .thenAsync(v -> v + 1)
                .thenSync(v -> v * 2)
                .get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("cross-err", e.getCause().getMessage());
        }
    }

    @Test
    public void testCrossTypeRecoveryInAsyncContinuedInSync() throws Exception {
        String result = SyncResult.<Integer>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handleAsync((v, ex) -> ex != null ? -1 : v)   // AsyncResult recovers
            .thenSync(v -> "result=" + v)                    // SyncResult continues
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("result=-1", result);
    }

    // =========================================================================
    // Delayed completion (chain set up before value arrives)
    // =========================================================================

    @Test
    public void testChainOnIncompleteResult() throws Exception {
        SyncResult<Integer> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        SyncResult<String> chained = r.then(v -> v * 2).then(v -> "delayed=" + v);
        assertFalse(chained.isDone());
        r.complete(5);
        assertEquals("delayed=10", chained.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testMultiChainOnSameIncompleteResult() throws Exception {
        SyncResult<Integer> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        SyncResult<Integer> branch1 = r.then(v -> v + 1);
        SyncResult<Integer> branch2 = r.then(v -> v + 2);
        SyncResult<Integer> branch3 = r.then(v -> v + 3);
        r.complete(10);
        assertEquals(Integer.valueOf(11), branch1.get(TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Integer.valueOf(12), branch2.get(TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Integer.valueOf(13), branch3.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testDelayedCompletionWithError() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        SyncResult<String> chained = r.then(v -> v + " chained");
        r.completeExceptionally(new RuntimeException("delayed-err"));
        try {
            chained.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("delayed-err", e.getCause().getMessage());
        }
    }

    @Test
    public void testDelayedCompletionWithAcceptAsync() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        AtomicReference<String> captured = new AtomicReference<>();
        AsyncResult<Void> chained = r.thenAcceptAsync(captured::set);
        r.complete("delayed-value");
        chained.get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("delayed-value", captured.get());
    }

    @Test
    public void testDelayedCompletionWithCombine() throws Exception {
        SyncResult<Integer> a = SyncResult.create(TestExecutionProvider.INSTANCE);
        SyncResult<Integer> b = SyncResult.create(TestExecutionProvider.INSTANCE);
        SyncResult<Integer> combined = a.thenCombine(b, Integer::sum);
        assertFalse(combined.isDone());
        a.complete(3);
        assertFalse(combined.isDone());
        b.complete(7);
        assertEquals(Integer.valueOf(10), combined.get(TIMEOUT, TimeUnit.SECONDS));
    }

    // =========================================================================
    // Nested composition
    // =========================================================================

    @Test
    public void testNestedThenCompose() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "a")
            .thenCompose(v1 ->
                SyncResult.completed(TestExecutionProvider.INSTANCE, v1 + "b")
                    .thenCompose(v2 ->
                        CompletableFuture.completedFuture(v2 + "c")
                    )
            );
        assertEquals("abc", r.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testComposeWithDelayedInner() throws Exception {
        SyncResult<Integer> inner = SyncResult.create(TestExecutionProvider.INSTANCE);
        SyncResult<String> outer = SyncResult.completed(TestExecutionProvider.INSTANCE, 5)
            .thenCompose(v -> inner.then(i -> v + i))
            .then(v -> "sum=" + v);
        assertFalse(outer.isDone());
        inner.complete(10);
        assertEquals("sum=15", outer.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testComposeReturningFailedInner() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .thenCompose(v -> {
                CompletableFuture<String> f = new CompletableFuture<>();
                f.completeExceptionally(new RuntimeException("inner-fail"));
                return f;
            });
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("inner-fail", e.getCause().getMessage());
        }
    }

    @Test
    public void testComposeAsyncWithNestedSyncResult() throws Exception {
        AsyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "start")
            .thenComposeAsync(v -> {
                CompletableFuture<String> inner = new CompletableFuture<>();
                inner.complete(v + "-end");
                return inner;
            });
        assertEquals("start-end", r.get(TIMEOUT, TimeUnit.SECONDS));
    }

    // =========================================================================
    // unwrapThrowable behavior
    // =========================================================================

    @Test
    public void testExceptionallySyncUnwrapsCompletionException() throws Exception {
        SyncResult<String> source = SyncResult.create(TestExecutionProvider.INSTANCE);
        source.completeExceptionally(new CompletionException(new IllegalStateException("unwrapped")));

        AtomicReference<Throwable> captured = new AtomicReference<>();
        String result = source.exceptionallySync(ex -> {
            captured.set(ex);
            return "recovered";
        }).get(TIMEOUT, TimeUnit.SECONDS);

        assertEquals("recovered", result);
        assertTrue(captured.get() instanceof IllegalStateException);
        assertEquals("unwrapped", captured.get().getMessage());
    }

    @Test
    public void testExceptionallyAsyncUnwrapsCompletionException() throws Exception {
        SyncResult<String> source = SyncResult.create(TestExecutionProvider.INSTANCE);
        source.completeExceptionally(new CompletionException(new IllegalArgumentException("unwrap-async")));

        AtomicReference<Throwable> captured = new AtomicReference<>();
        String result = source.exceptionallyAsync(ex -> {
            captured.set(ex);
            return "async-recovered";
        }).get(TIMEOUT, TimeUnit.SECONDS);

        assertEquals("async-recovered", result);
        assertTrue(captured.get() instanceof IllegalArgumentException);
        assertEquals("unwrap-async", captured.get().getMessage());
    }

    @Test
    public void testFromStageUnwrapsCompletionException() throws Exception {
        CompletableFuture<String> stage = new CompletableFuture<>();
        stage.completeExceptionally(new CompletionException(new IllegalStateException("fromStage-unwrap")));

        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, stage);
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("fromStage-unwrap", e.getCause().getMessage());
        }
    }

    @Test
    public void testUnwrapsNestedCompletionAndExecutionExceptions() throws Exception {
        SyncResult<String> source = SyncResult.create(TestExecutionProvider.INSTANCE);
        source.completeExceptionally(
            new CompletionException(
                new ExecutionException(
                    new IllegalStateException("deeply-nested")
                )
            )
        );

        AtomicReference<Throwable> captured = new AtomicReference<>();
        source.exceptionallySync(ex -> {
            captured.set(ex);
            return "ok";
        }).get(TIMEOUT, TimeUnit.SECONDS);

        assertTrue(captured.get() instanceof IllegalStateException);
        assertEquals("deeply-nested", captured.get().getMessage());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedExceptionallyStillWorks() throws Exception {
        SyncResult<String> source = SyncResult.create(TestExecutionProvider.INSTANCE);
        source.completeExceptionally(new RuntimeException("dep-err"));

        String result = source.exceptionally(ex -> "dep-recovered")
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("dep-recovered", result);
    }

    // =========================================================================
    // ExecutionProvider preservation through chains
    // =========================================================================

    @Test
    public void testExecutionProviderPreservedThroughThen() {
        SyncResult<Integer> r = SyncResult.completed(TestExecutionProvider.INSTANCE, 1).then(v -> v);
        assertSame(TestExecutionProvider.INSTANCE, r.getExecutionProvider());
    }

    @Test
    public void testExecutionProviderPreservedThroughThenAsync() {
        AsyncResult<Integer> r = SyncResult.completed(TestExecutionProvider.INSTANCE, 1).thenAsync(v -> v);
        assertSame(TestExecutionProvider.INSTANCE, r.getExecutionProvider());
    }

    @Test
    public void testExecutionProviderPreservedThroughHandle() {
        SyncResult<Integer> r = SyncResult.completed(TestExecutionProvider.INSTANCE, 1).handle((v, ex) -> v);
        assertSame(TestExecutionProvider.INSTANCE, r.getExecutionProvider());
    }

    @Test
    public void testExecutionProviderPreservedAfterMultipleChains() {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, 1)
            .then(v -> v + 1)
            .then(String::valueOf)
            .thenCompose(v -> CompletableFuture.completedFuture(v + "!"));
        assertSame(TestExecutionProvider.INSTANCE, r.getExecutionProvider());
    }

    // =========================================================================
    // Null value handling in chains
    // =========================================================================

    @Test
    public void testThenWithNullValue() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, (String) null)
            .then(v -> v == null ? "was-null" : v);
        assertEquals("was-null", r.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testThenReturningNull() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .then(v -> (String) null);
        assertNull(r.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testThenAcceptWithNullValue() throws Exception {
        AtomicBoolean receivedNull = new AtomicBoolean();
        SyncResult.completed(TestExecutionProvider.INSTANCE, (String) null)
            .thenAccept(v -> receivedNull.set(v == null))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertTrue(receivedNull.get());
    }

    @Test
    public void testHandleWithNullValueNoError() throws Exception {
        String result = SyncResult.completed(TestExecutionProvider.INSTANCE, (String) null)
            .handle((v, ex) -> v == null && ex == null ? "null-no-err" : "other")
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("null-no-err", result);
    }

    // =========================================================================
    // Alias methods (thenAcceptSync/thenRunSync/thenComposeSync/handleSync/etc)
    // =========================================================================

    @Test
    public void testThenAcceptSyncIsAlias() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "alias")
            .thenAcceptSync(captured::set)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("alias", captured.get());
    }

    @Test
    public void testThenRunSyncIsAlias() throws Exception {
        AtomicBoolean ran = new AtomicBoolean();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "x")
            .thenRunSync(() -> ran.set(true))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertTrue(ran.get());
    }

    @Test
    public void testThenComposeSyncIsAlias() throws Exception {
        String result = SyncResult.completed(TestExecutionProvider.INSTANCE, "a")
            .thenComposeSync(v -> CompletableFuture.completedFuture(v + "b"))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("ab", result);
    }

    @Test
    public void testHandleSyncIsAlias() throws Exception {
        String result = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .handleSync((v, ex) -> v + "!")
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("ok!", result);
    }

    @Test
    public void testWhenCompleteSyncIsAlias() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "sync-alias")
            .whenCompleteSync((v, ex) -> captured.set(v))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("sync-alias", captured.get());
    }

    @Test
    public void testThenCombineSyncIsAlias() throws Exception {
        SyncResult<Integer> a = SyncResult.completed(TestExecutionProvider.INSTANCE, 3);
        SyncResult<Integer> b = SyncResult.completed(TestExecutionProvider.INSTANCE, 7);
        int result = a.thenCombineSync(b, Integer::sum).get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(10, result);
    }

    @Test
    public void testAcceptWithCustomExecutor() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "custom")
            .accept(captured::set, Runnable::run)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("custom", captured.get());
    }

    @Test
    public void testAcceptAsyncWithCustomExecutor() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "async-custom")
            .acceptAsync(captured::set, Runnable::run)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals("async-custom", captured.get());
    }

    // =========================================================================
    // completeValue / fail helpers
    // =========================================================================

    @Test
    public void testCompleteValue() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        assertTrue(r.completeValue("v"));
        assertEquals("v", r.get(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testFail() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        assertTrue(r.fail(new RuntimeException("failed")));
        try {
            r.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("failed", e.getCause().getMessage());
        }
    }
}
