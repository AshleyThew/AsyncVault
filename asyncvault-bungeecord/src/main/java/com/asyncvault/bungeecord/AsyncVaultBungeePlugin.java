package com.asyncvault.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import com.asyncvault.api.service.AsyncVaultServiceRegistry;

/**
 * BungeeCord/Waterfall plugin entry point for AsyncVault.
 *
 * <p>For cross-plugin service registration use {@link AsyncVaultServiceRegistry#getInstance()}.
 * Any plugin can register or look up arbitrary services without depending on AsyncVault
 * internals — only the API jar is required.
 */
public class AsyncVaultBungeePlugin extends Plugin {

    private static AsyncVaultBungeePlugin instance;
    private ExecutionProvider executionProvider;

    @Override
    public void onEnable() {
        instance = this;
        executionProvider = new BungeeExecutionProvider(this);
        ExecutionProviderContext.set(executionProvider);
        getLogger().info("AsyncVault loaded.");
    }

    @Override
    public void onDisable() {
        AsyncVaultServiceRegistry.getInstance().clear();
        ExecutionProviderContext.clear();
        getLogger().info("AsyncVault unloaded.");
    }

    /** Returns the AsyncVault BungeeCord plugin instance. */
    public static AsyncVaultBungeePlugin getInstance() { 
        return instance;
    }

    /** Returns the global service registry for cross-plugin service lookup and registration. */
    public static AsyncVaultServiceRegistry getServiceRegistry() {
        return AsyncVaultServiceRegistry.getInstance();
    }

    /** Returns platform sync/async execution hooks for provider implementations. */
    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }
}
