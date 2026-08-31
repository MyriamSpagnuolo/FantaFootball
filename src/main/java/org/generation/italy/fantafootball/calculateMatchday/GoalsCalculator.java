package org.generation.italy.fantafootball.calculateMatchday;

/**
 * Converte il punteggio fantacalcistico nei gol della squadra.
 *
 * Fasce: meno di 67 = 0 gol, poi un gol ogni 5 punti.
 */
public final class GoalsCalculator {

    private static final double FIRST_GOAL_THRESHOLD = 67.0;
    private static final double POINTS_PER_GOAL = 5.0;

    private GoalsCalculator() {
    }

    public static int calculateGoals(double points) {
        if (!Double.isFinite(points) || points < 0) {
            throw new IllegalArgumentException("Points must be a finite, non-negative number");
        }
        if (points < FIRST_GOAL_THRESHOLD) {
            return 0;
        }

        return 1 + (int) Math.floor((points - FIRST_GOAL_THRESHOLD) / POINTS_PER_GOAL);
    }
}
