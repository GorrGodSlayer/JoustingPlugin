package com.jousting;

/**
 * Pure damage-balancing math (no Bukkit types) so it can be unit-tested.
 * All values are Minecraft damage points (2 points = 1 heart).
 *
 * Lance damage is intentionally independent of Sharpness and Strength: a charge's
 * damage comes only from momentum, horse speed tier and lance tier, then is clamped
 * to a hard cap so a single hit can never one-shot a full-health (20-point) player.
 */
public final class DamageCalculator {

    private DamageCalculator() {}

    /** Clamp the momentum-scaled base damage to [0, hardCap]. */
    public static double clamp(double base, double hardCap) {
        if (base < 0) return 0.0;
        return Math.min(base, hardCap);
    }
}
