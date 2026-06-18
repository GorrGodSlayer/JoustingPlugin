package com.jousting;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoustingCommand implements CommandExecutor, TabCompleter {
    private final JoustingPlugin plugin;
    private final JoustingConfig config;
    private final LanceItems lances;

    public JoustingCommand(JoustingPlugin plugin, JoustingConfig config, LanceItems lances) {
        this.plugin = plugin;
        this.config = config;
        this.lances = lances;
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
            sender.sendMessage("§e/jousting give <iron|gold|diamond> §7- Get a lance");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            config.reload();
            sender.sendMessage("§aJoustingPlugin configuration reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can receive a lance.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /jousting give <iron|gold|diamond>");
                return true;
            }
            LanceTier tier = LanceTier.fromName(args[1]);
            if (tier == null) {
                sender.sendMessage("§cUnknown lance tier: " + args[1] + " (iron|gold|diamond)");
                return true;
            }
            ItemStack lance = lances.create(tier, config);
            player.getInventory().addItem(lance);
            sender.sendMessage("§aGave you a " + tier.getDisplayName() + ".");
            return true;
        }

        sender.sendMessage("§cUnknown command! Use /jousting for help.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("reload", "give"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(Arrays.asList("iron", "gold", "diamond"), args[1]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String prefix) {
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase().startsWith(prefix.toLowerCase())) out.add(o);
        }
        return out;
    }
}
