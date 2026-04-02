package com.asyncvault.sponge;

import com.asyncvault.api.execution.ExecutionProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import java.util.concurrent.Executor;

/**
 * Execution provider for Sponge.
 */
public final class SpongeExecutionProvider implements ExecutionProvider {

    private final PluginContainer pluginContainer;

    public SpongeExecutionProvider(PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Override
    public Executor syncExecutor() {
        return task -> Sponge.server().scheduler().submit(
            Task.builder().plugin(pluginContainer).execute(task).build()
        );
    }

    @Override
    public Executor asyncExecutor() {
        return task -> Sponge.asyncScheduler().submit(
            Task.builder().plugin(pluginContainer).execute(task).build()
        );
    }
}
