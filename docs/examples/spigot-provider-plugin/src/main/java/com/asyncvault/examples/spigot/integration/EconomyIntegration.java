package com.asyncvault.examples.spigot.integration;

import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.examples.spigot.providers.EssentialsEconomyProvider;
import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class EconomyIntegration {

    private EconomyIntegration() {
    }

    public static void register(JavaPlugin plugin) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Essentials")) {
            plugin.getLogger().warning("Essentials not found. Economy provider not registered.");
            return;
        }

        Essentials essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            plugin.getLogger().warning("Essentials not available. Economy provider not registered.");
            return;
        }

        EconomyProvider provider = new EssentialsEconomyProvider(essentials);
        Bukkit.getServicesManager().register(EconomyProvider.class, provider, plugin, ServicePriority.Normal);
        plugin.getLogger().info("Registered AsyncVault economy provider: " + provider.getName());
    }
}
