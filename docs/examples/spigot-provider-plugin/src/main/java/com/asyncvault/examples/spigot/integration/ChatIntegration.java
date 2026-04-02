package com.asyncvault.examples.spigot.integration;

import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.examples.spigot.providers.PlaceholderChatProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatIntegration {

    private ChatIntegration() {
    }

    public static void register(JavaPlugin plugin) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            plugin.getLogger().warning("PlaceholderAPI not found. Chat provider not registered.");
            return;
        }

        ChatProvider provider = new PlaceholderChatProvider();
        Bukkit.getServicesManager().register(ChatProvider.class, provider, plugin, ServicePriority.Normal);
        plugin.getLogger().info("Registered AsyncVault chat provider: " + provider.getName());
    }
}
