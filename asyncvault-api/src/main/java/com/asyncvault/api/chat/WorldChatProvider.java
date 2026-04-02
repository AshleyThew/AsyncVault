package com.asyncvault.api.chat;

import com.asyncvault.api.execution.ExecutionProvider;

import java.util.Objects;

/**
 * Base chat provider variant bound to a specific world scope.
 */
public abstract class WorldChatProvider extends ChatProvider {

    private final String worldName;

    protected WorldChatProvider(String worldName) {
        super();
        this.worldName = Objects.requireNonNull(worldName, "worldName");
    }

    protected WorldChatProvider(ExecutionProvider executionProvider, String worldName) {
        super(executionProvider);
        this.worldName = Objects.requireNonNull(worldName, "worldName");
    }

    /**
     * @return world name this provider instance is scoped to
     */
    public String getWorldName() {
        return worldName;
    }
}
