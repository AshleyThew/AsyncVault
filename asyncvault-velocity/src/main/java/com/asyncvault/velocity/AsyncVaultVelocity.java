package com.asyncvault.velocity;

import com.asyncvault.api.execution.ExecutionProvider;
import com.asyncvault.api.execution.ExecutionProviderContext;
import com.asyncvault.api.service.AsyncVaultServiceRegistry;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

/**
 * AsyncVault entry point for Velocity.
 * Plugin metadata is declared in velocity-plugin.json (built with the correct
 * version at compile time via processResources) rather than in a @Plugin
 * annotation, so that the version is injected from project.version / JitPack tag.
 *
 * <p>For cross-plugin service registration use {@link AsyncVaultServiceRegistry#getInstance()}.
 * Any plugin can register or look up arbitrary services — not limited to AsyncVault.
 */
public class AsyncVaultVelocity {

    private static AsyncVaultVelocity instance;
    private final ProxyServer server;
    private final Logger logger;
    private ExecutionProvider executionProvider;

    @Inject
    public AsyncVaultVelocity(ProxyServer server, Logger logger) {
        instance = this;
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        executionProvider = new VelocityExecutionProvider(this, server);
        ExecutionProviderContext.set(executionProvider);
        logger.info("AsyncVault loaded.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        ExecutionProviderContext.clear();
        logger.info("AsyncVault unloaded.");
    }

    public static AsyncVaultVelocity getInstance() {
        return instance;
    }

    public ProxyServer getProxyServer() {
        return server;
    }

    /** Returns the global service registry for cross-plugin service lookup and registration. */
    public static AsyncVaultServiceRegistry getServiceRegistry() {
        return AsyncVaultServiceRegistry.getInstance();
    }

    public ExecutionProvider getExecutionProvider() {
        return executionProvider;
    }
}
