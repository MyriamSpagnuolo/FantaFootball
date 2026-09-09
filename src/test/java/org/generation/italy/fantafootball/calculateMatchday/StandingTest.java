package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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

    @Test
    void doesNotDuplicatePointsWhenTheSameMatchIsRecalculated() {
        Team home = new Team();
        Team away = new Team();
        LeagueMatch match = new LeagueMatch();
        match.setHomeTeam(home);
        match.setAwayTeam(away);

        standing.updateStanding(match, 72, 66);
        standing.updateStanding(match, 72, 66);

        assertEquals(3, home.getTotalPoints());
        assertEquals(0, away.getTotalPoints());
        assertEquals(0, BigDecimal.valueOf(72).compareTo(match.getHomeScore()));
    }

    @Test
    void ordersByLeaguePointsThenByFantasyPoints() {
        Team first = new Team("First", null, null);
        Team second = new Team("Second", null, null);
        first.setTotalPoints(6);
        second.setTotalPoints(6);

        LeagueMatch match = new LeagueMatch();
        match.setHomeTeam(first);
        match.setAwayTeam(second);
        match.setHomeScore(BigDecimal.valueOf(80));
        match.setAwayScore(BigDecimal.valueOf(70));

        List<Standing.StandingEntry> result = standing.calculateStanding(
                List.of(second, first), List.of(match));

        assertEquals(first, result.get(0).team());
        assertEquals(80.0, result.get(0).fantasyPoints());
        assertEquals(second, result.get(1).team());
    }
}
