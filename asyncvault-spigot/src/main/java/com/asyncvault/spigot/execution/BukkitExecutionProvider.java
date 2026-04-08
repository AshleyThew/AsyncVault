package com.asyncvault.spigot.execution;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import com.asyncvault.api.execution.ExecutionProvider;

import java.util.concurrent.Executor;

public final class BukkitExecutionProvider implements ExecutionProvider {

    private final Plugin plugin;

    public BukkitExecutionProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Executor syncExecutor() {
        return task -> {
            if (Bukkit.isPrimaryThread()) {
                task.run();
                return;
            }
            Bukkit.getScheduler().runTask(plugin, task);
        };
    }

    @Override
    public Executor asyncExecutor() {
        return task -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }
}
