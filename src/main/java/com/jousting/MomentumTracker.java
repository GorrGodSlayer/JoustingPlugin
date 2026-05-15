package com.jousting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public static double getMomentumDistance(UUID playerUUID) {
        PlayerMomentum data = momentumData.getOrDefault(playerUUID, new PlayerMomentum());
        return data.totalDistance;
    }

    public static void resetMomentum(UUID playerUUID) {
        momentumData.remove(playerUUID);
    }

    public static class PlayerMomentum {
        public Double lastX;
        public Double lastY;
        public Double lastZ;
        public double totalDistance = 0;
    }
}