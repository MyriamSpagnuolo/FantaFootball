package org.generation.italy.fantafootball.calculateMatchday;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoalsCalculatorTest {
    @Test
    void calculatesGoalsAtEveryBoundary() {
        assertEquals(0, GoalsCalculator.calculateGoals(0));
        assertEquals(0, GoalsCalculator.calculateGoals(66.99));
        assertEquals(1, GoalsCalculator.calculateGoals(67));
        assertEquals(1, GoalsCalculator.calculateGoals(71.99));
        assertEquals(2, GoalsCalculator.calculateGoals(72));
        assertEquals(3, GoalsCalculator.calculateGoals(77));
    }

    @Test
    void rejectsInvalidPoints() {
        assertThrows(IllegalArgumentException.class, () -> GoalsCalculator.calculateGoals(-1));
        assertThrows(IllegalArgumentException.class, () -> GoalsCalculator.calculateGoals(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> GoalsCalculator.calculateGoals(Double.POSITIVE_INFINITY));
    }
}
