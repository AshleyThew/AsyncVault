package com.asyncvault.examples.spigot.providers;

import com.asyncvault.api.AsyncResult;
import com.asyncvault.api.chat.ChatProvider;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public final class PlaceholderChatProvider extends ChatProvider {

    @Override
    public String getName() {
        return "PlaceholderAPI";
    }

    @Override
    public boolean supportsAsyncOperations() {
        return true;
    }

    @Override
    public String getPlayerPrefix(UUID uuid) {
        throw new UnsupportedOperationException("Use getPlayerPrefixAsync");
    }

    @Override
    public String getPlayerSuffix(UUID uuid) {
        throw new UnsupportedOperationException("Use getPlayerSuffixAsync");
    }

    @Override
    public AsyncResult<String> getPlayerPrefixAsync(UUID uuid) {
        return getExecutionProvider().supplySync(() -> resolve(uuid, "%player_prefix%")).asAsync();
    }

    @Override
    public AsyncResult<String> getPlayerSuffixAsync(UUID uuid) {
        return getExecutionProvider().supplySync(() -> resolve(uuid, "%player_suffix%")).asAsync();
    }

    private String resolve(UUID uuid, String placeholder) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String value = PlaceholderAPI.setPlaceholders(player, placeholder);
        return value == null ? "" : value;
    }
}
