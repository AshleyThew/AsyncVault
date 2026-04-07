package com.asyncvault.fabric;

import net.fabricmc.api.ModInitializer;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import com.asyncvault.api.service.AsyncVaultServiceRegistry;

/**
 * Fabric mod entry point for AsyncVault.
 *
 * <p>For cross-mod service registration use {@link AsyncVaultServiceRegistry#getInstance()}.
 * Any mod can register or look up arbitrary services without depending on AsyncVault
 * internals — only the API jar is required.
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
     /** Returns the AsyncVault Fabric mod instance. */
    public static AsyncVaultFabric getInstance() {
        return instance;
    }

    /** Returns the global service registry for cross-mod service lookup and registration. */
    public static AsyncVaultServiceRegistry getServiceRegistry() {
        return AsyncVaultServiceRegistry.getInstance();
    }

    /** Returns platform sync/async execution hooks for provider implementations. */
    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }
}
