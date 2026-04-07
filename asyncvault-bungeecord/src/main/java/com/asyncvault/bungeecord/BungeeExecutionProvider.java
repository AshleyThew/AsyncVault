package com.asyncvault.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import com.asyncvault.api.execution.ExecutionProvider;

import java.util.concurrent.Executor;

public final class BungeeExecutionProvider implements ExecutionProvider {

    private final Plugin plugin;

    public BungeeExecutionProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * BungeeCord/Waterfall has no main thread. Both executors route through
     * BungeeCord's own managed scheduler thread pool so tasks respect the
     * plugin lifecycle and are cancelled cleanly on proxy shutdown.
     */
    @Override
    public Executor syncExecutor() {
        return task -> plugin.getProxy().getScheduler().runAsync(plugin, task);
    }

    @Override
    public Executor asyncExecutor() {
        return task -> plugin.getProxy().getScheduler().runAsync(plugin, task);
    }
}
