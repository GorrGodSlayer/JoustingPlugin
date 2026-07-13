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
 *
 * A charge is anchored where it started: momentum can never exceed the straight-line
 * displacement from that anchor, so circling a target builds only as much momentum as
 * the circle is wide — a full charge requires an actual run-up.
 */
public class MomentumTask extends BukkitRunnable {
    // A move bigger than this in one tick is a teleport, not a horse; don't credit it as momentum.
    private static final double MAX_PLAUSIBLE_TICK_DISTANCE = 2.0;

    private final JoustingPlugin plugin;
    private final JoustingConfig config;
    private final MomentumBarManager barManager;
    private final Map<UUID, Location> lastLoc = new HashMap<>();
    private final Map<UUID, Location> chargeAnchor = new HashMap<>();

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
                forget(id);
                barManager.remove(id);
                continue;
            }

            Location now = horse.getLocation();
            Location prev = lastLoc.put(id, now.clone());
            if (prev == null || prev.getWorld() != now.getWorld()) {
                chargeAnchor.remove(id); // new ride or world change: charge starts over
                continue;
            }

            double moved = prev.distance(now);
            if (moved > MAX_PLAUSIBLE_TICK_DISTANCE) {
                chargeAnchor.remove(id);
                continue; // teleported: don't credit the jump, resample from here next tick
            }

            LanceTier tier = LanceTier.fromMaterial(player.getInventory().getItemInMainHand().getType());
            double full = config.getFullMomentumDistance();
            double min = config.getMinimumMomentumDistance();

            if (moved >= config.getMinimumRunSpeed()) {
                // (Re-)anchor where this charge began; a spent/decayed charge restarts here.
                Location anchor = chargeAnchor.get(id);
                if (anchor == null || MomentumTracker.getMomentumDistance(id) <= 0) {
                    anchor = prev.clone();
                    chargeAnchor.put(id, anchor);
                }
                double displacement = anchor.distance(now);
                MomentumTracker.setDistance(id, MomentumTracker.chargeMomentum(
                        MomentumTracker.getMomentumDistance(id), moved, displacement, full));
            } else {
                MomentumTracker.decay(id, config.getMomentumDecayPerTick());
            }

            barManager.update(player, MomentumTracker.getFraction(id, min, full), tier);
        }
    }

    /** Drop a player's cached samples (used when they quit or dismount). */
    public void forget(UUID playerId) {
        lastLoc.remove(playerId);
        chargeAnchor.remove(playerId);
    }
}
