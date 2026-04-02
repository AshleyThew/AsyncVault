package com.asyncvault.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import com.asyncvault.spigot.execution.ExecutionProviderSupport;

/**
 * Main AsyncVault plugin class for Spigot/Paper.
 */
public class AsyncVaultPlugin extends JavaPlugin {

    private static AsyncVaultPlugin instance;
    private ExecutionProvider executionProvider;

    @Override
    public void onLoad() {
        instance = this;
        executionProvider = ExecutionProviderSupport.createProvider(this);
        ExecutionProviderContext.set(executionProvider);
        getLogger().info("AsyncVault loaded.");
    }

    @Override
    public void onDisable() {
        ExecutionProviderContext.clear();
        getLogger().info("AsyncVault unloaded.");
    }

    /**
     * Gets the AsyncVault plugin instance.
     */
    public static AsyncVaultPlugin getInstance() {
        return instance;
    }

    /**
     * Gets platform sync/async execution hooks for provider implementations.
     */
    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }
}
