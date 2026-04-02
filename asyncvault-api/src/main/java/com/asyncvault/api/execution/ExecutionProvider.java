package com.asyncvault.api.execution;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.SyncResult;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Platform execution hooks for provider implementations.
 *
 * <p>Use {@link #syncExecutor()} when work must run on the server thread,
 * and {@link #asyncExecutor()} for non-blocking/background operations.
 */
public interface ExecutionProvider {

    /**
     * @return executor that runs tasks on the platform sync/server thread
     */
    Executor syncExecutor();

    /**
     * @return executor that runs tasks asynchronously
     */
    Executor asyncExecutor();

    default void runSync(Runnable task) {
        syncExecutor().execute(task);
    }

    default void runAsync(Runnable task) {
        asyncExecutor().execute(task);
    }

    default <T> AsyncResult<T> supplyAsync(Supplier<T> supplier) {
        return AsyncResult.supply(this, supplier);
    }

    default <T> SyncResult<T> supplySync(Supplier<T> supplier) {
        return SyncResult.supply(this, supplier);
    }
}
