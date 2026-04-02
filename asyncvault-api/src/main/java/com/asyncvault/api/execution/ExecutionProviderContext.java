package com.asyncvault.api.execution;

import java.util.Objects;

/**
 * Holds the active platform execution provider for the current runtime.
 */
public final class ExecutionProviderContext {

    private static volatile ExecutionProvider executionProvider;

    private ExecutionProviderContext() {
    }

    public static void set(ExecutionProvider provider) {
        executionProvider = Objects.requireNonNull(provider, "provider");
    }

    public static ExecutionProvider get() {
        return executionProvider;
    }

    public static ExecutionProvider require() {
        ExecutionProvider provider = executionProvider;
        if (provider == null) {
            throw new IllegalStateException("ExecutionProvider is not set. Set ExecutionProviderContext during platform bootstrap.");
        }
        return provider;
    }

    public static void clear() {
        executionProvider = null;
    }
}
