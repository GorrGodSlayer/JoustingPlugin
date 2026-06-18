package com.jousting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks accumulated travel distance ("momentum") per riding player.
 * Distance only accrues while the listener decides the horse is moving fast enough.
 */
public class MomentumTracker {
    private static final Map<UUID, PlayerMomentum> momentumData = new HashMap<>();

    public static void trackLocation(UUID playerUUID, double x, double y, double z) {
        PlayerMomentum data = momentumData.getOrDefault(playerUUID, new PlayerMomentum());

        if (data.lastX == null) {
            data.lastX = x;
            data.lastY = y;
            data.lastZ = z;
        } else {
            double distance = Math.sqrt(
                Math.pow(x - data.lastX, 2) +
                Math.pow(y - data.lastY, 2) +
                Math.pow(z - data.lastZ, 2)
            );
            data.totalDistance += distance;
            data.lastX = x;
            data.lastY = y;
            data.lastZ = z;
        }

        momentumData.put(playerUUID, data);
    }

    /** Add a movement delta (blocks) to the player's momentum total. */
    public static void addDistance(UUID playerUUID, double distance) {
        PlayerMomentum data = momentumData.getOrDefault(playerUUID, new PlayerMomentum());
        data.totalDistance += distance;
        momentumData.put(playerUUID, data);
    }

    /** Add movement but never exceed {@code cap}, so momentum reflects the current charge. */
    public static void addDistanceCapped(UUID playerUUID, double distance, double cap) {
        PlayerMomentum data = momentumData.getOrDefault(playerUUID, new PlayerMomentum());
        data.totalDistance = Math.min(data.totalDistance + distance, cap);
        momentumData.put(playerUUID, data);
    }

    /** Bleed momentum down by {@code amount} (blocks); clears the entry at zero. */
    public static void decay(UUID playerUUID, double amount) {
        PlayerMomentum data = momentumData.get(playerUUID);
        if (data == null) return;
        data.totalDistance -= amount;
        if (data.totalDistance <= 0) momentumData.remove(playerUUID);
        else momentumData.put(playerUUID, data);
    }

    public static double getMomentumDistance(UUID playerUUID) {
        PlayerMomentum data = momentumData.get(playerUUID);
        return data == null ? 0.0 : data.totalDistance;
    }

    /** Fill fraction in [0,1] for UI, given the configured full-momentum distance. */
    public static double getFraction(UUID playerUUID, double fullDistance) {
        return HorseSpeedTier.momentumFraction(getMomentumDistance(playerUUID), fullDistance);
    }

    public static boolean hasData(UUID playerUUID) {
        return momentumData.containsKey(playerUUID);
    }

    public static void resetMomentum(UUID playerUUID) {
        momentumData.remove(playerUUID);
    }

    public static void clearAll() {
        momentumData.clear();
    }

    public static class PlayerMomentum {
        public Double lastX;
        public Double lastY;
        public Double lastZ;
        public double totalDistance = 0;
    }
}
