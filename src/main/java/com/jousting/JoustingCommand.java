package com.jousting;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoustingCommand implements CommandExecutor {
    private final JoustingPlugin plugin;
    private final JoustingConfig config;

    public JoustingCommand(JoustingPlugin plugin, JoustingConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jousting.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6=== JoustingPlugin Commands ===");
            sender.sendMessage("§e/jousting reload §7- Reload plugin configuration");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            config.reload();
            sender.sendMessage("§aJoustingPlugin configuration reloaded!");
            return true;
        }

        sender.sendMessage("§cUnknown command! Use /jousting for help.");
        return true;
    }
}