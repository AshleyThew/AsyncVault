package com.asyncvault.spigot.execution;

import org.bukkit.plugin.Plugin;
import com.asyncvault.api.execution.ExecutionProvider;

import java.util.concurrent.Executor;

public final class PaperExecutionProvider implements ExecutionProvider {

    private final Plugin plugin;

    public PaperExecutionProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Executor syncExecutor() {
        return task -> plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public Executor asyncExecutor() {
        return task -> plugin.getServer().getAsyncScheduler().runNow(plugin, ignored -> task.run());
    }
}
