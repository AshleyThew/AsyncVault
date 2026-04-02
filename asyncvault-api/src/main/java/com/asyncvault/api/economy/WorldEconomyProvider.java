package com.asyncvault.api.economy;

import com.asyncvault.api.execution.ExecutionProvider;

import java.util.Objects;

/**
 * Base economy provider variant bound to a specific world scope.
 */
public abstract class WorldEconomyProvider extends EconomyProvider {

    private final String worldName;

    protected WorldEconomyProvider(String worldName) {
        super();
        this.worldName = Objects.requireNonNull(worldName, "worldName");
    }

    protected WorldEconomyProvider(ExecutionProvider executionProvider, String worldName) {
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
