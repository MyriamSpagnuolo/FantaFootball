package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TeamMatchStatsTest {
    @Test
    void appliesDefensiveModifierUsingTopThreeDefendersAndGoalkeeper() {
        Lineup lineup = mock(Lineup.class);
        when(lineup.isDefensive()).thenReturn(true);

        PlayerMatchStats goalkeeper = player("P", 7.0);
        PlayerMatchStats defenderOne = player("D", 7.0);
        PlayerMatchStats defenderTwo = player("D", 7.0);
        PlayerMatchStats defenderThree = player("D", 7.0);
        PlayerMatchStats defenderFour = player("D", 5.0);

        MatchdayLineup matchdayLineup = new MatchdayLineup(
                lineup,
                List.of(goalkeeper, defenderOne, defenderTwo, defenderThree, defenderFour));

        assertEquals(6, new TeamMatchStats().calculateModBonus(matchdayLineup));
    }

    @Test
    void doesNotApplyDefensiveModifierToAnOffensiveLineup() {
        Lineup lineup = mock(Lineup.class);
        when(lineup.isDefensive()).thenReturn(false);

        assertEquals(0, new TeamMatchStats().calculateModBonus(
                new MatchdayLineup(lineup, List.of())));
    }

    private static PlayerMatchStats player(String position, double vote) {
        LineupPlayer lineupPlayer = mock(LineupPlayer.class);
        when(lineupPlayer.isStarter()).thenReturn(true);
        when(lineupPlayer.getPosition()).thenReturn(position);

        PlayerMatchStats stats = mock(PlayerMatchStats.class);
        when(stats.getLineupPlayer()).thenReturn(lineupPlayer);
        when(stats.getVote()).thenReturn(vote);
        return stats;
    }
}
