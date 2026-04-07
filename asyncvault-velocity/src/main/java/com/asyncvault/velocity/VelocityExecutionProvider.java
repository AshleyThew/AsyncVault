package com.asyncvault.velocity;

import com.asyncvault.api.execution.ExecutionProvider;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.concurrent.Executor;

public final class VelocityExecutionProvider implements ExecutionProvider {

    private final Object plugin;
    private final ProxyServer server;

    public VelocityExecutionProvider(Object plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    /**
     * Velocity has no main thread. Both executors route through Velocity's own
     * managed scheduler thread pool so tasks respect the plugin lifecycle and
     * are cancelled cleanly on proxy shutdown.
     */
    @Override
    public Executor syncExecutor() {
        return task -> server.getScheduler().buildTask(plugin, task::run).schedule();
    }

    @Override
    public Executor asyncExecutor() {
        return task -> server.getScheduler().buildTask(plugin, task::run).schedule();
    }
}
