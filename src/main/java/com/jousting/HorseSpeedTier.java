package com.jousting;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class HorseSpeedTier {
    
    public static boolean isHighTier(Horse horse, JoustingConfig config) {
        double speed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
        return speed >= config.getHighTierSpeedThreshold();
    }

    public static double getMaxDamage(Horse horse, JoustingConfig config) {
        if (isHighTier(horse, config)) {
            return config.getHighTierMaxDamage();
        }
        return config.getLowTierMaxDamage();
    }

    public static double calculateDamage(double momentum, double minDistance, double maxDamage) {
        if (momentum < minDistance) {
            return 0;
        }
        
        // Normalize momentum to 0-1 range, capped at minDistance
        double normalizedMomentum = Math.min(momentum / (minDistance * 3), 1.0);
        return maxDamage * normalizedMomentum;
    }
}