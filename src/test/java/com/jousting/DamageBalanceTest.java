package com.jousting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DamageBalanceTest {

    @Test
    void clampsToHardCap() {
        assertEquals(9.0, DamageCalculator.clamp(20.0, 9.0), 1e-9);
        assertEquals(9.0, DamageCalculator.clamp(9.0, 9.0), 1e-9);
    }

    @Test
    void belowCapPassesThrough() {
        assertEquals(5.5, DamageCalculator.clamp(5.5, 9.0), 1e-9);
    }

    @Test
    void negativeFloorsAtZero() {
        assertEquals(0.0, DamageCalculator.clamp(-3.0, 9.0), 1e-9);
    }

    @Test
    void neverOneShotsFullHealthPlayer() {
        // Worst case (huge base) is still clamped well under a player's 20 health points.
        assertTrue(DamageCalculator.clamp(1000.0, 9.0) < 20.0);
    }
}
