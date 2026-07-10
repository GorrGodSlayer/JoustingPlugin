package com.jousting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks accumulated travel distance ("momentum") per riding player.
 * Distance only accrues while {@link MomentumTask} decides the horse is moving fast enough.
 */
public final class MomentumTracker {
    private static final Map<UUID, PlayerMomentum> momentumData = new HashMap<>();

    private MomentumTracker() {}

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

    /** Fill fraction in [0,1] for UI, along the same ramp the damage curve uses. */
    public static double getFraction(UUID playerUUID, double minDistance, double fullDistance) {
        return HorseSpeedTier.momentumFraction(getMomentumDistance(playerUUID), minDistance, fullDistance);
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

    private static class PlayerMomentum {
        double totalDistance = 0;
    }
}
