package com.asyncvault.examples.spigot;

import com.asyncvault.api.chat.ChatProvider;
import com.asyncvault.api.economy.EconomyProvider;
import com.asyncvault.api.permission.PermissionProvider;
import com.asyncvault.examples.spigot.integration.EconomyIntegration;
import com.asyncvault.examples.spigot.integration.PermissionIntegration;
import com.asyncvault.examples.spigot.integration.ChatIntegration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AsyncVaultHookPlugin extends JavaPlugin {

    private static final long DEMO_DELAY_MS = 3500L;

    @Override
    public void onEnable() {
        EconomyIntegration.register(this);
        PermissionIntegration.register(this);
        ChatIntegration.register(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!"avhooks".equalsIgnoreCase(command.getName())) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }

        Player player = (Player) sender;

        EconomyProvider economy = Bukkit.getServicesManager().load(EconomyProvider.class);
        PermissionProvider permission = Bukkit.getServicesManager().load(PermissionProvider.class);
        ChatProvider chat = Bukkit.getServicesManager().load(ChatProvider.class);

        if (economy == null) {
            player.sendMessage("[AsyncVault] Economy provider not found.");
        } else {
            economy.getBalanceAsync(player.getUniqueId())
                .then(balance -> delayValue(balance, DEMO_DELAY_MS))
                .thenSync(balance -> {
                    player.sendMessage("[AsyncVault] Economy(" + economy.getName() + ") balance=" + economy.format(balance));
                    return null;
                })
                .exceptionally(error -> {
                    player.sendMessage("[AsyncVault] Economy error: " + error.getMessage());
                    return null;
                });
        }

        if (permission == null) {
            player.sendMessage("[AsyncVault] Permission provider not found.");
        } else {
            permission.hasPermissionAsync(player.getUniqueId(), "avhooks.status")
                .then(has -> delayValue(has, DEMO_DELAY_MS))
                .thenSync(has -> {
                    player.sendMessage("[AsyncVault] Permission(" + permission.getName() + ") avhooks.status=" + has);
                    return null;
                })
                .exceptionally(error -> {
                    player.sendMessage("[AsyncVault] Permission error: " + error.getMessage());
                    return null;
                });
        }

        if (chat == null) {
            player.sendMessage("[AsyncVault] Chat provider not found.");
        } else {
            chat.getPlayerPrefixAsync(player.getUniqueId())
                .thenCombine(chat.getPlayerSuffixAsync(player.getUniqueId()),
                    (prefix, suffix) -> "prefix='" + prefix + "' suffix='" + suffix + "'")
                .then(meta -> delayValue(meta, DEMO_DELAY_MS))
                .thenSync(meta -> {
                    player.sendMessage("[AsyncVault] Chat(" + chat.getName() + ") " + meta);
                    return null;
                })
                .exceptionally(error -> {
                    player.sendMessage("[AsyncVault] Chat error: " + error.getMessage());
                    return null;
                });
        }

        return true;
    }

    private static <T> T delayValue(T value, long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return value;
    }

}
