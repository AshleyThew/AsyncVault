package com.asyncvault.fabric;

import net.fabricmc.api.ModInitializer;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;

/**
 * Fabric mod entry point for AsyncVault.
 */
public class AsyncVaultFabric implements ModInitializer {

    private static AsyncVaultFabric instance;
    private ExecutionProvider executionProvider;

    @Override
    public void onInitialize() {
        instance = this;
        executionProvider = new FabricExecutionProvider();
        ExecutionProviderContext.set(executionProvider);
        System.out.println("AsyncVault loaded.");
    }

    /**
     * Gets the AsyncVault Fabric instance.
     */
    public static AsyncVaultFabric getInstance() {
        return instance;
    }

    /**
     * Gets platform sync/async execution hooks for provider implementations.
     */
    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }
}
