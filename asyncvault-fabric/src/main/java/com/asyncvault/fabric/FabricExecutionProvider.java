package com.asyncvault.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import com.asyncvault.api.execution.ExecutionProvider;

import net.minecraft.Util;

import java.util.concurrent.Executor;

/**
 * Execution provider for Fabric.
 */
public final class FabricExecutionProvider implements ExecutionProvider {

    private final Executor asyncExecutor = Util.backgroundExecutor();
    private volatile MinecraftServer server;

    public FabricExecutionProvider() {
        ServerLifecycleEvents.SERVER_STARTED.register(startedServer -> this.server = startedServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(stoppedServer -> this.server = null);
    }

    @Override
    public Executor syncExecutor() {
        return task -> {
            MinecraftServer current = server;
            if (current == null) {
                task.run();
                return;
            }
            current.execute(task);
        };
    }

    @Override
    public Executor asyncExecutor() {
        return asyncExecutor;
    }
}
