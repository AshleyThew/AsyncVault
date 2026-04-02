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
 * CompletableFuture-backed async result that routes chained work through the
 * configured ExecutionProvider.
 *
 * <p>Example usage:
 * <pre>
 * // Future-based pattern
 * AsyncResult<Double> balance = economyProvider.getBalanceAsync(player);
 * balance.thenAccept(bal -> player.sendMessage("Balance: $" + bal));
 *
 * // Consumer callback pattern
 * economyProvider.getBalanceAsync(player).acceptAsync(bal -> {
 *     player.sendMessage("Balance: $" + bal);
 * });
 *
 * // Blocking pattern (use sparingly)
 * double balance = economyProvider.getBalanceAsync(player).get(5, TimeUnit.SECONDS);
 * </pre>
 *
 * @param <T> The result type
 */
public abstract class AsyncResult<T> extends CompletableFuture<T> {

    private final ExecutionProvider executionProvider;

    protected AsyncResult(ExecutionProvider executionProvider) {
        this.executionProvider = Objects.requireNonNull(executionProvider, "executionProvider");
    }

    protected AsyncResult() {
        this(ExecutionProviderContext.require());
    }

    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }

    public <U> AsyncResult<U> then(Function<? super T, ? extends U> fn) {
        return fromStage(executionProvider, super.thenApplyAsync(fn, executionProvider.asyncExecutor()));
    }

    public <U> SyncResult<U> thenSync(Function<? super T, ? extends U> fn) {
        return SyncResult.fromStage(executionProvider, super.thenApplyAsync(fn, executionProvider.syncExecutor()));
    }

    public <U, V> AsyncResult<V> thenCombine(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return fromStage(executionProvider, super.thenCombineAsync(other, fn, executionProvider.asyncExecutor()));
    }

    public <U, V> SyncResult<V> thenCombineSync(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return SyncResult.fromStage(executionProvider, super.thenCombineAsync(other, fn, executionProvider.syncExecutor()));
    }

    public AsyncResult<Void> thenAccept(Consumer<? super T> consumer) {
        return fromStage(executionProvider, super.thenAcceptAsync(consumer, executionProvider.asyncExecutor()));
    }

    public SyncResult<Void> thenAcceptSync(Consumer<? super T> consumer) {
        return SyncResult.fromStage(executionProvider, super.thenAcceptAsync(consumer, executionProvider.syncExecutor()));
    }

    public AsyncResult<Void> thenRun(Runnable action) {
        return fromStage(executionProvider, super.thenRunAsync(action, executionProvider.asyncExecutor()));
    }

    public SyncResult<Void> thenRunSync(Runnable action) {
        return SyncResult.fromStage(executionProvider, super.thenRunAsync(action, executionProvider.syncExecutor()));
    }

    public <U> AsyncResult<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return fromStage(executionProvider, super.thenComposeAsync(fn, executionProvider.asyncExecutor()));
    }

    public <U> SyncResult<U> thenComposeSync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return SyncResult.fromStage(
            executionProvider,
            super.thenApplyAsync(fn, executionProvider.syncExecutor()).thenCompose(Function.identity())
        );
    }

    public <U> AsyncResult<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return fromStage(executionProvider, super.handleAsync(fn, executionProvider.asyncExecutor()));
    }

    public <U> SyncResult<U> handleSync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return SyncResult.fromStage(executionProvider, super.handleAsync(fn, executionProvider.syncExecutor()));
    }

    public AsyncResult<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return fromStage(executionProvider, super.whenCompleteAsync(action, executionProvider.asyncExecutor()));
    }

    public SyncResult<T> whenCompleteSync(BiConsumer<? super T, ? super Throwable> action) {
        return SyncResult.fromStage(executionProvider, super.whenCompleteAsync(action, executionProvider.syncExecutor()));
    }

    @Override
    public AsyncResult<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return fromStage(executionProvider, super.exceptionally(fn));
    }

    public AsyncResult<Void> accept(Consumer<? super T> consumer) {
        return thenAccept(consumer);
    }

    public AsyncResult<Void> accept(Consumer<? super T> consumer, Executor executor) {
        return fromStage(executionProvider, super.thenAcceptAsync(consumer, executor));
    }

    public SyncResult<Void> acceptSync(Consumer<? super T> consumer) {
        return thenAcceptSync(consumer);
    }

    public SyncResult<Void> acceptSync(Consumer<? super T> consumer, Executor serverThreadExecutor) {
        return SyncResult.fromStage(executionProvider, super.thenAcceptAsync(consumer, serverThreadExecutor));
    }

    // Compatibility aliases
    public <U> AsyncResult<U> thenAsync(Function<? super T, ? extends U> fn) {
        return then(fn);
    }

    public AsyncResult<Void> thenAcceptAsync(Consumer<? super T> consumer) {
        return thenAccept(consumer);
    }

    public AsyncResult<Void> thenRunAsync(Runnable action) {
        return thenRun(action);
    }

    public <U> AsyncResult<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return thenCompose(fn);
    }

    public <U> AsyncResult<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return handle(fn);
    }

    public AsyncResult<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return whenComplete(action);
    }

    public AsyncResult<Void> acceptAsync(Consumer<? super T> consumer) {
        return accept(consumer);
    }

    public AsyncResult<Void> acceptAsync(Consumer<? super T> consumer, Executor executor) {
        return accept(consumer, executor);
    }

    public <U, V> AsyncResult<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return thenCombine(other, fn);
    }

    public SyncResult<T> asSync() {
        return SyncResult.fromStage(executionProvider, this);
    }

    public T getNowValue() {
        return join();
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

    /**
     * Creates a new completed AsyncResult with the given value.
     *
     * @param value The result value
     * @param <T> The result type
     * @return A completed async result
     */
    public static <T> AsyncResult<T> completed(T value) {
        return completed(ExecutionProviderContext.require(), value);
    }

    /**
     * Creates a new completed AsyncResult with the given value and provider.
     *
     * @param executionProvider The execution provider bound to this result
     * @param value The result value
     * @param <T> The result type
     * @return A completed async result
     */
    public static <T> AsyncResult<T> completed(ExecutionProvider executionProvider, T value) {
        SimpleAsyncResult<T> result = new SimpleAsyncResult<>(executionProvider);
        result.complete(value);
        return result;
    }

    /**
     * Creates a new AsyncResult failed with the given exception.
     *
     * @param exception The exception
     * @param <T> The result type
     * @return A failed async result
     */
    public static <T> AsyncResult<T> failedWith(Throwable exception) {
        return failedWith(ExecutionProviderContext.require(), exception);
    }

    /**
     * Creates a new AsyncResult failed with the given exception and provider.
     *
     * @param executionProvider The execution provider bound to this result
     * @param exception The exception
     * @param <T> The result type
     * @return A failed async result
     */
    public static <T> AsyncResult<T> failedWith(ExecutionProvider executionProvider, Throwable exception) {
        SimpleAsyncResult<T> result = new SimpleAsyncResult<>(executionProvider);
        result.completeExceptionally(exception);
        return result;
    }

    /**
     * Creates a new incomplete AsyncResult.
     *
     * @param <T> The result type
     * @return An incomplete async result
     */
    public static <T> AsyncResult<T> create() {
        return create(ExecutionProviderContext.require());
    }

    /**
     * Creates a new incomplete AsyncResult bound to the given provider.
     *
     * @param executionProvider The execution provider bound to this result
     * @param <T> The result type
     * @return An incomplete async result
     */
    public static <T> AsyncResult<T> create(ExecutionProvider executionProvider) {
        return new SimpleAsyncResult<>(executionProvider);
    }

    /**
     * Creates a new AsyncResult and completes it asynchronously via supplier.
     *
     * @param supplier The supplier to evaluate asynchronously
     * @param <T> The result type
     * @return An async result linked to the supplier execution
     */
    public static <T> AsyncResult<T> supplyAsync(Supplier<T> supplier) {
        return supply(supplier);
    }

    /**
     * Creates a new AsyncResult and completes it asynchronously via supplier.
     *
     * @param supplier The supplier to evaluate asynchronously
     * @param <T> The result type
     * @return An async result linked to the supplier execution
     */
    public static <T> AsyncResult<T> supply(Supplier<T> supplier) {
        ExecutionProvider provider = ExecutionProviderContext.require();
        return supply(provider, supplier);
    }

    /**
     * Creates a new AsyncResult and completes it asynchronously via supplier using a custom executor.
     *
     * @param supplier The supplier to evaluate asynchronously
     * @param executor The executor to run the supplier on
     * @param <T> The result type
     * @return An async result linked to the supplier execution
     */
    public static <T> AsyncResult<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        AsyncResult<T> result = create();
        CompletableFuture.supplyAsync(supplier, executor).whenComplete((value, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
            } else {
                result.complete(value);
            }
        });
        return result;
    }

    public static <T> AsyncResult<T> supply(ExecutionProvider executionProvider, Supplier<T> supplier) {
        AsyncResult<T> result = create(executionProvider);
        executionProvider.asyncExecutor().execute(() -> {
            try {
                result.complete(supplier.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    static <U> AsyncResult<U> fromStage(ExecutionProvider executionProvider, CompletionStage<U> stage) {
        AsyncResult<U> result = create(executionProvider);
        stage.whenComplete((value, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
            } else {
                result.complete(value);
            }
        });
        return result;
    }

    public static <U> AsyncResult<U> fromStage(CompletionStage<U> stage) {
        return fromStage(ExecutionProviderContext.require(), stage);
    }

    /**
     * Simple implementation of AsyncResult for direct usage.
     */
    public static class SimpleAsyncResult<T> extends AsyncResult<T> {
        public SimpleAsyncResult() {
            super();
        }

        public SimpleAsyncResult(ExecutionProvider executionProvider) {
            super(executionProvider);
        }
    }
}
