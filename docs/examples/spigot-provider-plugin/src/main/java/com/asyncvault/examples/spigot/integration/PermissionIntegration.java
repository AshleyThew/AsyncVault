package com.asyncvault.examples.spigot.integration;

import com.asyncvault.api.permission.PermissionProvider;
import com.asyncvault.examples.spigot.providers.LuckPermsPermissionProvider;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class PermissionIntegration {

    private PermissionIntegration() {
    }

    public static void register(JavaPlugin plugin) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            plugin.getLogger().warning("LuckPerms not found. Permission provider not registered.");
            return;
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            PermissionProvider provider = new LuckPermsPermissionProvider(luckPerms);
            Bukkit.getServicesManager().register(PermissionProvider.class, provider, plugin, ServicePriority.Normal);
            plugin.getLogger().info("Registered AsyncVault permission provider: " + provider.getName());
        } catch (IllegalStateException ex) {
            plugin.getLogger().warning("LuckPerms API was not ready. Permission provider not registered.");
        }
    }
}
