package com.asyncvault.spigot.execution;

import org.bukkit.plugin.Plugin;
import com.asyncvault.api.execution.ExecutionProvider;

import java.util.concurrent.Executor;

public final class FoliaExecutionProvider implements ExecutionProvider {

    private final Plugin plugin;

    public FoliaExecutionProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Executor syncExecutor() {
        return task -> plugin.getServer().getGlobalRegionScheduler().run(plugin, ignored -> task.run());
    }

    @Override
    public Executor asyncExecutor() {
        return task -> plugin.getServer().getAsyncScheduler().runNow(plugin, ignored -> task.run());
    }
}
