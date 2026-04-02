package com.asyncvault.spigot.execution;

import org.bukkit.plugin.Plugin;
import com.asyncvault.api.execution.ExecutionProvider;

/**
 * Detects platform scheduler support and creates the appropriate execution provider.
 */
public final class ExecutionProviderSupport {

    private ExecutionProviderSupport() {
    }

    public static ExecutionProvider createProvider(Plugin plugin) {
        if (supportsFolia(plugin)) {
            return new FoliaExecutionProvider(plugin);
        }
        if (supportsPaper(plugin)) {
            return new PaperExecutionProvider(plugin);
        }
        return new BukkitExecutionProvider(plugin);
    }

    public static boolean supportsFolia(Plugin plugin) {
        try {
            plugin.getServer().getClass().getMethod("getGlobalRegionScheduler");
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    public static boolean supportsPaper(Plugin plugin) {
        try {
            plugin.getServer().getClass().getMethod("getAsyncScheduler");
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }
}
