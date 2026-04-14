package com.asyncvault.api;

import com.asyncvault.api.execution.ExecutionProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Shared test ExecutionProvider that runs both sync and async on thread pools.
 */
public class TestExecutionProvider implements ExecutionProvider {

    public static final TestExecutionProvider INSTANCE = new TestExecutionProvider();

    private final Executor syncExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "test-sync");
        t.setDaemon(true);
        return t;
    });

    private final Executor asyncExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "test-async");
        t.setDaemon(true);
        return t;
    });

    @Override
    public Executor syncExecutor() {
        return syncExecutor;
    }

    @Override
    public Executor asyncExecutor() {
        return asyncExecutor;
    }
}
