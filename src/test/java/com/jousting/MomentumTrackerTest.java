package com.jousting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MomentumTrackerTest {

    @AfterEach
    void cleanup() {
        MomentumTracker.clearAll();
    }

    @Test
    void accumulatesDistance() {
        UUID id = UUID.randomUUID();
        MomentumTracker.trackLocation(id, 0, 0, 0); // first point sets origin, no distance
        assertEquals(0.0, MomentumTracker.getMomentumDistance(id), 1e-9);

        MomentumTracker.trackLocation(id, 3, 0, 4); // distance 5 from origin
        assertEquals(5.0, MomentumTracker.getMomentumDistance(id), 1e-9);

        MomentumTracker.trackLocation(id, 3, 0, 4); // no movement
        assertEquals(5.0, MomentumTracker.getMomentumDistance(id), 1e-9);
    }

    @Test
    void resetClearsMomentum() {
        UUID id = UUID.randomUUID();
        MomentumTracker.trackLocation(id, 0, 0, 0);
        MomentumTracker.trackLocation(id, 10, 0, 0);
        assertTrue(MomentumTracker.getMomentumDistance(id) > 0);

        MomentumTracker.resetMomentum(id);
        assertEquals(0.0, MomentumTracker.getMomentumDistance(id), 1e-9);
        assertFalse(MomentumTracker.hasData(id));
    }

    @Test
    void fractionTracksTowardFullDistance() {
        UUID id = UUID.randomUUID();
        MomentumTracker.trackLocation(id, 0, 0, 0);
        MomentumTracker.trackLocation(id, 7.5, 0, 0);
        assertEquals(0.5, MomentumTracker.getFraction(id, 15.0), 1e-9);
    }

    @Test
    void unknownPlayerIsZero() {
        assertEquals(0.0, MomentumTracker.getMomentumDistance(UUID.randomUUID()), 1e-9);
    }
}
