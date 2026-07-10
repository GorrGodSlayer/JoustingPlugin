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
    /**
     * No horse covers this many blocks in a single tick (the fastest tops out well under
     * one). A larger jump is a position discontinuity — a same-world teleport — not a
     * charge, and crediting it would fill the momentum bar instantly from a standstill.
     */
    private static final double MAX_PLAUSIBLE_TICK_DISTANCE = 2.0;

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
            if (moved > MAX_PLAUSIBLE_TICK_DISTANCE) {
                continue; // teleported: don't credit the jump, resample from here next tick
            }

            LanceTier tier = LanceTier.fromMaterial(player.getInventory().getItemInMainHand().getType());
            double full = config.getFullMomentumDistance();
            double min = config.getMinimumMomentumDistance();

            if (moved >= config.getMinimumRunSpeed()) {
                MomentumTracker.addDistanceCapped(id, moved, full); // grow, capped at "full"
            } else {
                MomentumTracker.decay(id, config.getMomentumDecayPerTick()); // bleed down when slow
            }

            barManager.update(player, MomentumTracker.getFraction(id, min, full), tier);
        }
    }

    /** Drop a player's cached location sample (used when they quit mid-ride). */
    public void forget(UUID playerId) {
        lastLoc.remove(playerId);
    }
}
