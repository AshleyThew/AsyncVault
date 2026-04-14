package com.asyncvault.api;

import com.asyncvault.api.execution.ExecutionProviderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public class SyncResultTest {

    @Before
    public void setUp() {
        ExecutionProviderContext.set(TestExecutionProvider.INSTANCE);
    }

    @After
    public void tearDown() {
        ExecutionProviderContext.clear();
    }

    // --- Factory methods ---

    @Test
    public void testCompleted() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok");
        assertTrue(r.isDone());
        assertEquals("ok", r.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void testCompletedWithContextProvider() throws Exception {
        SyncResult<Integer> r = SyncResult.completed(42);
        assertTrue(r.isDone());
        assertEquals(Integer.valueOf(42), r.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void testFailedWith() throws Exception {
        SyncResult<String> r = SyncResult.failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("fail"));
        assertTrue(r.isDone());
        try {
            r.get(1, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("fail", e.getCause().getMessage());
        }
    }

    @Test
    public void testFailedWithContextProvider() throws Exception {
        SyncResult<String> r = SyncResult.failedWith(new RuntimeException("fail2"));
        try {
            r.get(1, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("fail2", e.getCause().getMessage());
        }
    }

    @Test
    public void testCreate() {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        assertFalse(r.isDone());
        r.complete("done");
        assertTrue(r.isDone());
    }

    @Test
    public void testCreateWithContextProvider() {
        SyncResult<String> r = SyncResult.create();
        assertFalse(r.isDone());
    }

    @Test
    public void testSupply() throws Exception {
        SyncResult<String> r = SyncResult.supply(TestExecutionProvider.INSTANCE, () -> "supplied");
        assertEquals("supplied", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testSupplyWithContextProvider() throws Exception {
        SyncResult<String> r = SyncResult.supply(() -> "ctx-supplied");
        assertEquals("ctx-supplied", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testSupplyWithException() throws Exception {
        SyncResult<String> r = SyncResult.supply(TestExecutionProvider.INSTANCE, () -> {
            throw new RuntimeException("supplier-fail");
        });
        try {
            r.get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("supplier-fail", e.getCause().getMessage());
        }
    }

    // --- Chaining operators ---

    @Test
    public void testThen() throws Exception {
        SyncResult<Integer> r = SyncResult.completed(TestExecutionProvider.INSTANCE, 5)
            .then(v -> v * 2);
        assertEquals(Integer.valueOf(10), r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testThenAsync() throws Exception {
        AsyncResult<Integer> r = SyncResult.completed(TestExecutionProvider.INSTANCE, 5)
            .thenAsync(v -> v + 1);
        assertEquals(Integer.valueOf(6), r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testThenAccept() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "hello")
            .thenAccept(captured::set);
        r.get(2, TimeUnit.SECONDS);
        assertEquals("hello", captured.get());
    }

    @Test
    public void testThenAcceptSync() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "sync-hello")
            .thenAcceptSync(captured::set);
        r.get(2, TimeUnit.SECONDS);
        assertEquals("sync-hello", captured.get());
    }

    @Test
    public void testThenAcceptAsync() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        AsyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "async-accept")
            .thenAcceptAsync(captured::set);
        r.get(2, TimeUnit.SECONDS);
        assertEquals("async-accept", captured.get());
    }

    @Test
    public void testThenRun() throws Exception {
        AtomicBoolean ran = new AtomicBoolean(false);
        SyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "x")
            .thenRun(() -> ran.set(true));
        r.get(2, TimeUnit.SECONDS);
        assertTrue(ran.get());
    }

    @Test
    public void testThenRunSync() throws Exception {
        AtomicBoolean ran = new AtomicBoolean(false);
        SyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "x")
            .thenRunSync(() -> ran.set(true));
        r.get(2, TimeUnit.SECONDS);
        assertTrue(ran.get());
    }

    @Test
    public void testThenRunAsync() throws Exception {
        AtomicBoolean ran = new AtomicBoolean(false);
        AsyncResult<Void> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "x")
            .thenRunAsync(() -> ran.set(true));
        r.get(2, TimeUnit.SECONDS);
        assertTrue(ran.get());
    }

    @Test
    public void testThenCompose() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "a")
            .thenCompose(v -> CompletableFuture.completedFuture(v + "b"));
        assertEquals("ab", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testThenComposeSync() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "x")
            .thenComposeSync(v -> CompletableFuture.completedFuture(v + "y"));
        assertEquals("xy", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testThenComposeAsync() throws Exception {
        AsyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "m")
            .thenComposeAsync(v -> CompletableFuture.completedFuture(v + "n"));
        assertEquals("mn", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testThenCombine() throws Exception {
        SyncResult<String> a = SyncResult.completed(TestExecutionProvider.INSTANCE, "hello");
        SyncResult<String> b = SyncResult.completed(TestExecutionProvider.INSTANCE, " world");
        SyncResult<String> r = a.thenCombine(b, (x, y) -> x + y);
        assertEquals("hello world", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testThenCombineSync() throws Exception {
        SyncResult<Integer> a = SyncResult.completed(TestExecutionProvider.INSTANCE, 3);
        SyncResult<Integer> b = SyncResult.completed(TestExecutionProvider.INSTANCE, 4);
        SyncResult<Integer> r = a.thenCombineSync(b, Integer::sum);
        assertEquals(Integer.valueOf(7), r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testThenCombineAsync() throws Exception {
        SyncResult<Integer> a = SyncResult.completed(TestExecutionProvider.INSTANCE, 2);
        SyncResult<Integer> b = SyncResult.completed(TestExecutionProvider.INSTANCE, 3);
        AsyncResult<Integer> r = a.thenCombineAsync(b, Integer::sum);
        assertEquals(Integer.valueOf(5), r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testHandle() throws Exception {
        SyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handle((val, ex) -> ex != null ? "recovered" : val);
        assertEquals("recovered", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testHandleSync() throws Exception {
        SyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handleSync((val, ex) -> "sync-recovered");
        assertEquals("sync-recovered", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testHandleAsync() throws Exception {
        AsyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .handleAsync((val, ex) -> "async-recovered");
        assertEquals("async-recovered", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testHandleSuccess() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "good")
            .handle((val, ex) -> ex != null ? "bad" : val);
        assertEquals("good", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testWhenComplete() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "val")
            .whenComplete((v, ex) -> captured.set(v));
        r.get(2, TimeUnit.SECONDS);
        assertEquals("val", captured.get());
    }

    @Test
    public void testWhenCompleteSync() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "sync-val")
            .whenCompleteSync((v, ex) -> captured.set(v));
        r.get(2, TimeUnit.SECONDS);
        assertEquals("sync-val", captured.get());
    }

    @Test
    public void testWhenCompleteAsync() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        AsyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "async-val")
            .whenCompleteAsync((v, ex) -> captured.set(v));
        r.get(2, TimeUnit.SECONDS);
        assertEquals("async-val", captured.get());
    }

    @Test
    public void testExceptionallySync() throws Exception {
        SyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .exceptionallySync(ex -> "sync-fallback");
        assertEquals("sync-fallback", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testExceptionallyAsync() throws Exception {
        AsyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .exceptionallyAsync(ex -> "async-fallback");
        assertEquals("async-fallback", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testExceptionallySyncNoError() throws Exception {
        SyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "ok")
            .exceptionallySync(ex -> "nope");
        assertEquals("ok", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testExceptionallyDeprecated() throws Exception {
        SyncResult<String> r = SyncResult.<String>failedWith(TestExecutionProvider.INSTANCE, new RuntimeException("err"))
            .exceptionally(ex -> "deprecated-fallback");
        assertEquals("deprecated-fallback", r.get(2, TimeUnit.SECONDS));
    }

    // --- Accept/AcceptSync aliases ---

    @Test
    public void testAccept() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "data")
            .accept(captured::set)
            .get(2, TimeUnit.SECONDS);
        assertEquals("data", captured.get());
    }

    @Test
    public void testAcceptSync() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "sync-data")
            .accept(captured::set)
            .get(2, TimeUnit.SECONDS);
        assertEquals("sync-data", captured.get());
    }

    @Test
    public void testAcceptWithExecutor() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "custom")
            .accept(captured::set, Runnable::run)
            .get(2, TimeUnit.SECONDS);
        assertEquals("custom", captured.get());
    }

    @Test
    public void testAcceptAsyncWithExecutor() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        SyncResult.completed(TestExecutionProvider.INSTANCE, "async-exec")
            .acceptAsync(captured::set, Runnable::run)
            .get(2, TimeUnit.SECONDS);
        assertEquals("async-exec", captured.get());
    }

    // --- Conversion ---

    @Test
    public void testAsAsync() throws Exception {
        AsyncResult<String> r = SyncResult.completed(TestExecutionProvider.INSTANCE, "convert")
            .asAsync();
        assertEquals("convert", r.get(2, TimeUnit.SECONDS));
    }

    // --- Complete/fail methods ---

    @Test
    public void testCompleteValue() {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        assertTrue(r.completeValue("v"));
        assertEquals("v", r.getNow(null));
    }

    @Test
    public void testFail() {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        assertTrue(r.fail(new RuntimeException("f")));
        assertTrue(r.isCompletedExceptionally());
    }

    @Test
    public void testGetExecutionProvider() {
        SyncResult<String> r = SyncResult.create(TestExecutionProvider.INSTANCE);
        assertSame(TestExecutionProvider.INSTANCE, r.getExecutionProvider());
    }

    // --- fromStage ---

    @Test
    public void testFromStage() throws Exception {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("staged");
        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, cf);
        assertEquals("staged", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testFromStageWithContextProvider() throws Exception {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("ctx-staged");
        SyncResult<String> r = SyncResult.fromStage(cf);
        assertEquals("ctx-staged", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testFromStageFailure() throws Exception {
        CompletableFuture<String> cf = new CompletableFuture<>();
        cf.completeExceptionally(new RuntimeException("stage-fail"));
        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, cf);
        try {
            r.get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertEquals("stage-fail", e.getCause().getMessage());
        }
    }

    @Test
    public void testFromStageCancelPropagation() {
        CompletableFuture<String> cf = new CompletableFuture<>();
        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, cf);
        r.cancel(true);
        assertTrue(r.isCancelled());
        assertTrue(cf.isCancelled());
    }

    @Test
    public void testFromStageCancelWhenAlreadyCompleted() {
        CompletableFuture<String> cf = new CompletableFuture<>();
        cf.complete("done");
        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, cf);
        assertEquals("done", r.join());
        boolean cancelled = r.cancel(true);
        assertFalse(cancelled);
        assertFalse(cf.isCancelled());
    }

    @Test
    public void testFromStageUnwrapsExecutionException() throws Exception {
        CompletableFuture<String> cf = new CompletableFuture<>();
        cf.completeExceptionally(new ExecutionException(new IllegalArgumentException("exec-unwrap")));
        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, cf);
        try {
            r.get(2, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals("exec-unwrap", e.getCause().getMessage());
        }
    }

    // --- fromStage with non-completable CompletionStage ---

    @Test
    public void testFromStageNonCompletableFuture() throws Exception {
        CompletableFuture<String> backing = new CompletableFuture<>();
        CompletionStage<String> stage = new NonCompletableStage<>(backing);
        backing.complete("non-cf");
        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, stage);
        assertEquals("non-cf", r.get(2, TimeUnit.SECONDS));
    }

    @Test
    public void testFromStageNonCompletableCancelDoesNotPropagate() {
        CompletableFuture<String> backing = new CompletableFuture<>();
        CompletionStage<String> stage = new NonCompletableStage<>(backing);
        SyncResult<String> r = SyncResult.fromStage(TestExecutionProvider.INSTANCE, stage);
        r.cancel(true);
        assertTrue(r.isCancelled());
        assertFalse(backing.isCancelled());
    }

    private static class NonCompletableStage<T> implements CompletionStage<T> {
        private final CompletableFuture<T> delegate;
        NonCompletableStage(CompletableFuture<T> delegate) { this.delegate = delegate; }
        @Override public <U> CompletionStage<U> thenApply(Function<? super T, ? extends U> fn) { return delegate.thenApply(fn); }
        @Override public CompletionStage<Void> thenAccept(Consumer<? super T> a) { return delegate.thenAccept(a); }
        @Override public CompletionStage<Void> thenRun(Runnable a) { return delegate.thenRun(a); }
        @Override public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> o, BiFunction<? super T, ? super U, ? extends V> fn) { return delegate.thenCombine(o, fn); }
        @Override public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> o, BiConsumer<? super T, ? super U> a) { return delegate.thenAcceptBoth(o, a); }
        @Override public CompletionStage<Void> runAfterBoth(CompletionStage<?> o, Runnable a) { return delegate.runAfterBoth(o, a); }
        @Override public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> o, Function<? super T, U> fn) { return delegate.applyToEither(o, fn); }
        @Override public CompletionStage<Void> acceptEither(CompletionStage<? extends T> o, Consumer<? super T> a) { return delegate.acceptEither(o, a); }
        @Override public CompletionStage<Void> runAfterEither(CompletionStage<?> o, Runnable a) { return delegate.runAfterEither(o, a); }
        @Override public <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) { return delegate.thenCompose(fn); }
        @Override public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn) { return delegate.exceptionally(fn); }
        @Override public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> a) { return delegate.whenComplete(a); }
        @Override public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) { return delegate.handle(fn); }
        @Override public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn) { return delegate.thenApplyAsync(fn); }
        @Override public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor e) { return delegate.thenApplyAsync(fn, e); }
        @Override public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> a) { return delegate.thenAcceptAsync(a); }
        @Override public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> a, Executor e) { return delegate.thenAcceptAsync(a, e); }
        @Override public CompletionStage<Void> thenRunAsync(Runnable a) { return delegate.thenRunAsync(a); }
        @Override public CompletionStage<Void> thenRunAsync(Runnable a, Executor e) { return delegate.thenRunAsync(a, e); }
        @Override public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> o, BiFunction<? super T, ? super U, ? extends V> fn) { return delegate.thenCombineAsync(o, fn); }
        @Override public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> o, BiFunction<? super T, ? super U, ? extends V> fn, Executor e) { return delegate.thenCombineAsync(o, fn, e); }
        @Override public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> o, BiConsumer<? super T, ? super U> a) { return delegate.thenAcceptBothAsync(o, a); }
        @Override public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> o, BiConsumer<? super T, ? super U> a, Executor e) { return delegate.thenAcceptBothAsync(o, a, e); }
        @Override public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> o, Runnable a) { return delegate.runAfterBothAsync(o, a); }
        @Override public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> o, Runnable a, Executor e) { return delegate.runAfterBothAsync(o, a, e); }
        @Override public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> o, Function<? super T, U> fn) { return delegate.applyToEitherAsync(o, fn); }
        @Override public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> o, Function<? super T, U> fn, Executor e) { return delegate.applyToEitherAsync(o, fn, e); }
        @Override public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> o, Consumer<? super T> a) { return delegate.acceptEitherAsync(o, a); }
        @Override public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> o, Consumer<? super T> a, Executor e) { return delegate.acceptEitherAsync(o, a, e); }
        @Override public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> o, Runnable a) { return delegate.runAfterEitherAsync(o, a); }
        @Override public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> o, Runnable a, Executor e) { return delegate.runAfterEitherAsync(o, a, e); }
        @Override public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) { return delegate.thenComposeAsync(fn); }
        @Override public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor e) { return delegate.thenComposeAsync(fn, e); }
        @Override public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> a) { return delegate.whenCompleteAsync(a); }
        @Override public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> a, Executor e) { return delegate.whenCompleteAsync(a, e); }
        @Override public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) { return delegate.handleAsync(fn); }
        @Override public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor e) { return delegate.handleAsync(fn, e); }
        @Override public CompletableFuture<T> toCompletableFuture() { throw new UnsupportedOperationException(); }
    }
}
