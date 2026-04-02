package com.asyncvault.sponge;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;

/**
 * AsyncVault plugin for Sponge (Sponge API 10+).
 */
@Plugin("asyncvault")
public class AsyncVaultSpongePlugin {

    private final PluginContainer container;
    private final ExecutionProvider executionProvider;

    public AsyncVaultSpongePlugin(PluginContainer container) {
        this.container = container;
        this.executionProvider = new SpongeExecutionProvider(container);
    }

    @Listener
    public void onLoad(ConstructPluginEvent event) {
        ExecutionProviderContext.set(executionProvider);
        container.logger().info("AsyncVault loaded.");
    }

    @Listener
    public void onStartup(StartedEngineEvent<org.spongepowered.api.Server> event) {
        container.logger().info("AsyncVault started.");
    }

    @Listener
    public void onShutdown(StoppingEngineEvent<org.spongepowered.api.Server> event) {
        ExecutionProviderContext.clear();
        container.logger().info("AsyncVault unloaded.");
    }

    /**
     * Gets the plugin container.
     */
    public PluginContainer getContainer() {
        return container;
    }

    /**
     * Gets platform sync/async execution hooks for provider implementations.
     */
    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }
}
