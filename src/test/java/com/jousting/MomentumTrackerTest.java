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
        MomentumTracker.addDistance(id, 5.0);
        assertEquals(5.0, MomentumTracker.getMomentumDistance(id), 1e-9);

        MomentumTracker.addDistance(id, 0.0); // no movement
        assertEquals(5.0, MomentumTracker.getMomentumDistance(id), 1e-9);
    }

    @Test
    void cappedAdditionNeverExceedsCap() {
        UUID id = UUID.randomUUID();
        MomentumTracker.addDistanceCapped(id, 10.0, 15.0);
        MomentumTracker.addDistanceCapped(id, 10.0, 15.0);
        assertEquals(15.0, MomentumTracker.getMomentumDistance(id), 1e-9);
    }

    @Test
    void decayBleedsDownAndClearsAtZero() {
        UUID id = UUID.randomUUID();
        MomentumTracker.addDistance(id, 5.0);
        MomentumTracker.decay(id, 2.0);
        assertEquals(3.0, MomentumTracker.getMomentumDistance(id), 1e-9);

        MomentumTracker.decay(id, 3.0);
        assertFalse(MomentumTracker.hasData(id));
    }

    @Test
    void resetClearsMomentum() {
        UUID id = UUID.randomUUID();
        MomentumTracker.addDistance(id, 10.0);
        assertTrue(MomentumTracker.getMomentumDistance(id) > 0);

        MomentumTracker.resetMomentum(id);
        assertEquals(0.0, MomentumTracker.getMomentumDistance(id), 1e-9);
        assertFalse(MomentumTracker.hasData(id));
    }

    @Test
    void fractionTracksTowardFullDistance() {
        UUID id = UUID.randomUUID();
        MomentumTracker.addDistance(id, 7.5);
        assertEquals(0.5, MomentumTracker.getFraction(id, 15.0), 1e-9);
    }

    @Test
    void unknownPlayerIsZero() {
        assertEquals(0.0, MomentumTracker.getMomentumDistance(UUID.randomUUID()), 1e-9);
    }
}
