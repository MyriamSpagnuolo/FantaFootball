package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Team;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandingTest {

    private final Standing standing = new Standing();

    @Test
    void assignsThreeOneAndZeroPoints() {
        assertEquals(3, standing.calculatePoints(2, 0));
        assertEquals(1, standing.calculatePoints(1, 1));
        assertEquals(0, standing.calculatePoints(0, 2));
    }

    @Test
    void updatesBothTeamsTotals() {
        Team home = new Team();
        Team away = new Team();
        home.setTotalPoints(4);
        away.setTotalPoints(7);

        standing.updateStanding(home, away, 3, 1);

        assertEquals(7, home.getTotalPoints());
        assertEquals(7, away.getTotalPoints());
    }
}
