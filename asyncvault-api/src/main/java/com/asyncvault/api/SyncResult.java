package com.asyncvault.api;

import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CompletableFuture-backed sync result that routes chained work through the
 * configured ExecutionProvider.
 *
 * @param <T> The result type
 */
public abstract class SyncResult<T> extends CompletableFuture<T> {

    private final ExecutionProvider executionProvider;

    protected SyncResult(ExecutionProvider executionProvider) {
        this.executionProvider = Objects.requireNonNull(executionProvider, "executionProvider");
    }

    protected SyncResult() {
        this(ExecutionProviderContext.require());
    }

    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }

    public <U> SyncResult<U> then(Function<? super T, ? extends U> fn) {
        return fromStage(executionProvider, super.thenApplyAsync(fn, executionProvider.syncExecutor()));
    }

    public <U> AsyncResult<U> thenAsync(Function<? super T, ? extends U> fn) {
        return AsyncResult.fromStage(executionProvider, super.thenApplyAsync(fn, executionProvider.asyncExecutor()));
    }

    public <U, V> SyncResult<V> thenCombine(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return fromStage(executionProvider, super.thenCombineAsync(other, fn, executionProvider.syncExecutor()));
    }

    public <U, V> AsyncResult<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return AsyncResult.fromStage(executionProvider, super.thenCombineAsync(other, fn, executionProvider.asyncExecutor()));
    }

    public SyncResult<Void> thenAccept(Consumer<? super T> consumer) {
        return fromStage(executionProvider, super.thenAcceptAsync(consumer, executionProvider.syncExecutor()));
    }

    public SyncResult<Void> thenAcceptSync(Consumer<? super T> consumer) {
        return thenAccept(consumer);
    }

    public AsyncResult<Void> thenAcceptAsync(Consumer<? super T> consumer) {
        return AsyncResult.fromStage(executionProvider, super.thenAcceptAsync(consumer, executionProvider.asyncExecutor()));
    }

    public SyncResult<Void> thenRun(Runnable action) {
        return fromStage(executionProvider, super.thenRunAsync(action, executionProvider.syncExecutor()));
    }

    public SyncResult<Void> thenRunSync(Runnable action) {
        return thenRun(action);
    }

    public AsyncResult<Void> thenRunAsync(Runnable action) {
        return AsyncResult.fromStage(executionProvider, super.thenRunAsync(action, executionProvider.asyncExecutor()));
    }

    public <U> SyncResult<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return fromStage(
            executionProvider,
            super.thenApplyAsync(fn, executionProvider.syncExecutor()).thenCompose(Function.identity())
        );
    }

    public <U> SyncResult<U> thenComposeSync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return thenCompose(fn);
    }

    public <U> AsyncResult<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return AsyncResult.fromStage(
            executionProvider,
            super.thenApplyAsync(fn, executionProvider.asyncExecutor()).thenCompose(Function.identity())
        );
    }

    public <U> SyncResult<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return fromStage(executionProvider, super.handleAsync(fn, executionProvider.syncExecutor()));
    }

    public <U> SyncResult<U> handleSync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return handle(fn);
    }

    public <U> AsyncResult<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return AsyncResult.fromStage(executionProvider, super.handleAsync(fn, executionProvider.asyncExecutor()));
    }

    public SyncResult<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return fromStage(executionProvider, super.whenCompleteAsync(action, executionProvider.syncExecutor()));
    }

    public SyncResult<T> whenCompleteSync(BiConsumer<? super T, ? super Throwable> action) {
        return whenComplete(action);
    }

    public AsyncResult<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return AsyncResult.fromStage(executionProvider, super.whenCompleteAsync(action, executionProvider.asyncExecutor()));
    }

    @Override
    public SyncResult<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return fromStage(executionProvider, super.exceptionally(fn));
    }

    public SyncResult<Void> accept(Consumer<? super T> consumer) {
        return thenAccept(consumer);
    }

    public SyncResult<Void> accept(Consumer<? super T> consumer, Executor executor) {
        return fromStage(executionProvider, super.thenAcceptAsync(consumer, executor));
    }

    public AsyncResult<Void> acceptAsync(Consumer<? super T> consumer) {
        return thenAcceptAsync(consumer);
    }

    public AsyncResult<Void> acceptAsync(Consumer<? super T> consumer, Executor executor) {
        return AsyncResult.fromStage(executionProvider, super.thenAcceptAsync(consumer, executor));
    }

    public <U, V> SyncResult<V> thenCombineSync(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return thenCombine(other, fn);
    }

    public AsyncResult<T> asAsync() {
        return AsyncResult.fromStage(executionProvider, this);
    }

    public boolean completeValue(T value) {
        return super.complete(value);
    }

    public boolean fail(Throwable ex) {
        return super.completeExceptionally(ex);
    }

    public boolean complete(T value) {
        return super.complete(value);
    }

    public boolean completeExceptionally(Throwable ex) {
        return super.completeExceptionally(ex);
    }

    public static <T> SyncResult<T> completed(T value) {
        return completed(ExecutionProviderContext.require(), value);
    }

    public static <T> SyncResult<T> completed(ExecutionProvider executionProvider, T value) {
        SimpleSyncResult<T> result = new SimpleSyncResult<>(executionProvider);
        result.complete(value);
        return result;
    }

    public static <T> SyncResult<T> failedWith(Throwable exception) {
        return failedWith(ExecutionProviderContext.require(), exception);
    }

    public static <T> SyncResult<T> failedWith(ExecutionProvider executionProvider, Throwable exception) {
        SimpleSyncResult<T> result = new SimpleSyncResult<>(executionProvider);
        result.completeExceptionally(exception);
        return result;
    }

    public static <T> SyncResult<T> create() {
        return create(ExecutionProviderContext.require());
    }

    public static <T> SyncResult<T> create(ExecutionProvider executionProvider) {
        return new SimpleSyncResult<>(executionProvider);
    }

    public static <T> SyncResult<T> supply(Supplier<T> supplier) {
        ExecutionProvider provider = ExecutionProviderContext.require();
        return supply(provider, supplier);
    }

    public static <T> SyncResult<T> supply(ExecutionProvider executionProvider, Supplier<T> supplier) {
        SyncResult<T> result = create(executionProvider);
        executionProvider.syncExecutor().execute(() -> {
            try {
                result.complete(supplier.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    static <U> SyncResult<U> fromStage(ExecutionProvider executionProvider, CompletionStage<U> stage) {
        SyncResult<U> result = create(executionProvider);
        stage.whenComplete((value, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
            } else {
                result.complete(value);
            }
        });
        return result;
    }

    public static <U> SyncResult<U> fromStage(CompletionStage<U> stage) {
        return fromStage(ExecutionProviderContext.require(), stage);
    }

    public static class SimpleSyncResult<T> extends SyncResult<T> {
        public SimpleSyncResult() {
            super();
        }

        public SimpleSyncResult(ExecutionProvider executionProvider) {
            super(executionProvider);
        }
    }
}