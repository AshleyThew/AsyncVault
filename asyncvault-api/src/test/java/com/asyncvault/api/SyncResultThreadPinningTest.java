package com.asyncvault.api;

import com.asyncvault.api.execution.ExecutionProviderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Verifies that every SyncResult chaining operator pins execution
 * to the correct executor thread (sync or async).
 *
 * Thread names: "test-sync" for syncExecutor, "test-async" for asyncExecutor.
 */
public class SyncResultThreadPinningTest {

    private static final long TIMEOUT = 3;
    private static final String ASYNC_THREAD = "test-async";
    private static final String SYNC_THREAD = "test-sync";

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }

    // =========================================================================
    // then / thenAsync
    // =========================================================================

    @Test
    public void testThenRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .then(v -> { thread.set(threadName()); return v; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenAsync(v -> { thread.set(threadName()); return v; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // thenAccept / thenAcceptSync / thenAcceptAsync
    // =========================================================================

    @Test
    public void testThenAcceptRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenAccept(v -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenAcceptSyncRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenAcceptSync(v -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenAcceptAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenAcceptAsync(v -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // thenRun / thenRunSync / thenRunAsync
    // =========================================================================

    @Test
    public void testThenRunRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenRun(() -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenRunSyncRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenRunSync(() -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenRunAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenRunAsync(() -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // thenCompose / thenComposeSync / thenComposeAsync
    // =========================================================================

    @Test
    public void testThenComposeFnRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenCompose(v -> {
                thread.set(threadName());
                return CompletableFuture.completedFuture(v);
            })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenComposeSyncFnRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenComposeSync(v -> {
                thread.set(threadName());
                return CompletableFuture.completedFuture(v);
            })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenComposeAsyncFnRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .thenComposeAsync(v -> {
                thread.set(threadName());
                return CompletableFuture.completedFuture(v);
            })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // thenCombine / thenCombineSync / thenCombineAsync
    // =========================================================================

    @Test
    public void testThenCombineRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult<String> other = SyncResult.completed(TestExecutionProvider.INSTANCE, "b");
        SyncResult.completed(TestExecutionProvider.INSTANCE, "a")
            .thenCombine(other, (a, b) -> { thread.set(threadName()); return a + b; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenCombineSyncRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult<String> other = SyncResult.completed(TestExecutionProvider.INSTANCE, "b");
        SyncResult.completed(TestExecutionProvider.INSTANCE, "a")
            .thenCombineSync(other, (a, b) -> { thread.set(threadName()); return a + b; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testThenCombineAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult<String> other = SyncResult.completed(TestExecutionProvider.INSTANCE, "b");
        SyncResult.completed(TestExecutionProvider.INSTANCE, "a")
            .thenCombineAsync(other, (a, b) -> { thread.set(threadName()); return a + b; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // handle / handleSync / handleAsync
    // =========================================================================

    @Test
    public void testHandleRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .handle((v, ex) -> { thread.set(threadName()); return v; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testHandleSyncRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .handleSync((v, ex) -> { thread.set(threadName()); return v; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testHandleAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .handleAsync((v, ex) -> { thread.set(threadName()); return v; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    @Test
    public void testHandleOnErrorRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handle((v, ex) -> { thread.set(threadName()); return "recovered"; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testHandleAsyncOnErrorRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handleAsync((v, ex) -> { thread.set(threadName()); return "recovered"; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // whenComplete / whenCompleteSync / whenCompleteAsync
    // =========================================================================

    @Test
    public void testWhenCompleteRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .whenComplete((v, ex) -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testWhenCompleteSyncRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .whenCompleteSync((v, ex) -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testWhenCompleteAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .whenCompleteAsync((v, ex) -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // exceptionallySync / exceptionallyAsync
    // =========================================================================

    @Test
    public void testExceptionallySyncRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .exceptionallySync(ex -> { thread.set(threadName()); return "ok"; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testExceptionallyAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .exceptionallyAsync(ex -> { thread.set(threadName()); return "ok"; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // accept / acceptAsync with custom executor
    // =========================================================================

    @Test
    public void testAcceptRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .accept(v -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testAcceptAsyncRunsOnAsyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .acceptAsync(v -> thread.set(threadName()))
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    @Test
    public void testAcceptWithCustomExecutor() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .accept(v -> thread.set(threadName()), Runnable::run)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertNotNull(thread.get());
    }

    @Test
    public void testAcceptAsyncWithCustomExecutor() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .acceptAsync(v -> thread.set(threadName()), Runnable::run)
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertNotNull(thread.get());
    }

    // =========================================================================
    // Multi-step chains alternate threads correctly
    // =========================================================================

    @Test
    public void testChainAlternatesAsyncAndSync() throws Exception {
        AtomicReference<String> t1 = new AtomicReference<>();
        AtomicReference<String> t2 = new AtomicReference<>();
        AtomicReference<String> t3 = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, 1)
            .then(v -> { t1.set(threadName()); return v; })          // sync
            .thenAsync(v -> { t2.set(threadName()); return v; })     // async
            .thenSync(v -> { t3.set(threadName()); return v; })      // sync
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, t1.get());
        assertEquals(ASYNC_THREAD, t2.get());
        assertEquals(SYNC_THREAD, t3.get());
    }

    @Test
    public void testChainMultipleSyncStayOnSync() throws Exception {
        AtomicReference<String> t1 = new AtomicReference<>();
        AtomicReference<String> t2 = new AtomicReference<>();
        AtomicReference<String> t3 = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, 1)
            .then(v -> { t1.set(threadName()); return v; })
            .then(v -> { t2.set(threadName()); return v; })
            .then(v -> { t3.set(threadName()); return v; })
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, t1.get());
        assertEquals(SYNC_THREAD, t2.get());
        assertEquals(SYNC_THREAD, t3.get());
    }

    @Test
    public void testChainMultipleAsyncStayOnAsync() throws Exception {
        AtomicReference<String> t1 = new AtomicReference<>();
        AtomicReference<String> t2 = new AtomicReference<>();
        AtomicReference<String> t3 = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, 1)
            .thenAsync(v -> { t1.set(threadName()); return v; })
            .then(v -> { t2.set(threadName()); return v; })     // AsyncResult.then → async
            .then(v -> { t3.set(threadName()); return v; })     // AsyncResult.then → async
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, t1.get());
        assertEquals(ASYNC_THREAD, t2.get());
        assertEquals(ASYNC_THREAD, t3.get());
    }

    @Test
    public void testHandleAsyncThenSyncPinsCorrectly() throws Exception {
        AtomicReference<String> t1 = new AtomicReference<>();
        AtomicReference<String> t2 = new AtomicReference<>();
        SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handleAsync((v, ex) -> { t1.set(threadName()); return "recovered"; })  // async
            .thenSync(v -> { t2.set(threadName()); return v; })                      // sync
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, t1.get());
        assertEquals(SYNC_THREAD, t2.get());
    }

    @Test
    public void testWhenCompleteAsyncThenAcceptSyncPinsCorrectly() throws Exception {
        AtomicReference<String> t1 = new AtomicReference<>();
        AtomicReference<String> t2 = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "v")
            .whenCompleteAsync((v, ex) -> t1.set(threadName()))   // async
            .thenAcceptSync(v -> t2.set(threadName()))             // sync
            .get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, t1.get());
        assertEquals(SYNC_THREAD, t2.get());
    }

    // =========================================================================
    // Delayed completion still pins correctly
    // =========================================================================

    @Test
    public void testDelayedThenPinsToSyncThread() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult<String> chained = r.then(v -> { thread.set(threadName()); return v; });
        r.complete("late");
        chained.get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testDelayedThenAsyncPinsToAsyncThread() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        AtomicReference<String> thread = new AtomicReference<>();
        AsyncResult<String> chained = r.thenAsync(v -> { thread.set(threadName()); return v; });
        r.complete("late");
        chained.get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    @Test
    public void testDelayedHandlePinsToSyncThread() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult<String> chained = r.handle((v, ex) -> { thread.set(threadName()); return v; });
        r.complete("late");
        chained.get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testDelayedHandleAsyncPinsToAsyncThread() throws Exception {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        AtomicReference<String> thread = new AtomicReference<>();
        AsyncResult<String> chained = r.handleAsync((v, ex) -> { thread.set(threadName()); return v; });
        r.complete("late");
        chained.get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(ASYNC_THREAD, thread.get());
    }

    // =========================================================================
    // supply pins correctly
    // =========================================================================

    @Test
    public void testSupplyRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult<String> r = SyncResult.supply(TestExecutionProvider.INSTANCE, () -> {
            thread.set(threadName());
            return "supplied";
        });
        r.get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }

    @Test
    public void testSupplyWithContextRunsOnSyncThread() throws Exception {
        AtomicReference<String> thread = new AtomicReference<>();
        SyncResult<String> r = SyncResult.supply(() -> {
            thread.set(threadName());
            return "supplied";
        });
        r.get(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(SYNC_THREAD, thread.get());
    }
}
