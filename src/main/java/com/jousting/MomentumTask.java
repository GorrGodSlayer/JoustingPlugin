package com.jousting;

import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Polls every tick for players riding a horse and builds momentum from how far the
 * horse actually moved since last tick. This avoids relying on movement events
 * (PlayerMoveEvent doesn't fire for passengers). Drives the momentum BossBar.
 *
 * Momentum grows (capped) while the horse moves fast and bleeds down smoothly when it
 * slows, so the bar reflects the current charge rather than lifetime distance, and a
 * single laggy/turning tick won't wipe it.
 */
public class MomentumTask extends BukkitRunnable {
    private final JoustingPlugin plugin;
    private final JoustingConfig config;
    private final MomentumBarManager barManager;
    private final Map<UUID, Location> lastLoc = new HashMap<>();

    public MomentumTask(JoustingPlugin plugin, JoustingConfig config, MomentumBarManager barManager) {
        this.plugin = plugin;
        this.config = config;
        this.barManager = barManager;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID id = player.getUniqueId();

            if (!(player.getVehicle() instanceof Horse horse)) {
                lastLoc.remove(id);
                barManager.remove(id);
                continue;
            }

            Location now = horse.getLocation();
            Location prev = lastLoc.put(id, now.clone());
            if (prev == null || prev.getWorld() != now.getWorld()) {
                continue; // first sample this ride
            }

            double moved = prev.distance(now);
            LanceTier tier = LanceTier.fromMaterial(player.getInventory().getItemInMainHand().getType());
            double full = config.getFullMomentumDistance();

            if (moved >= config.getMinimumRunSpeed()) {
                MomentumTracker.addDistanceCapped(id, moved, full); // grow, capped at "full"
            } else {
                MomentumTracker.decay(id, config.getMomentumDecayPerTick()); // bleed down when slow
            }

            barManager.update(player, MomentumTracker.getFraction(id, full), tier);
        }
    }

    /** Drop a player's cached location sample (used when they quit mid-ride). */
    public void forget(UUID playerId) {
        lastLoc.remove(playerId);
    }
}
