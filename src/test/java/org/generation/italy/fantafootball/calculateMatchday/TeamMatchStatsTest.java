package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void doesNotApplyDefensiveModifierToAThreeDefenderLineup() {
        Lineup lineup = mock(Lineup.class);
        when(lineup.isDefensive()).thenReturn(true);

        MatchdayLineup matchdayLineup = new MatchdayLineup(
                lineup,
                List.of(
                        player("P", 7.0),
                        player("D", 7.0),
                        player("D", 7.0),
                        player("D", 7.0)));

        assertEquals(0, new TeamMatchStats().calculateModBonus(matchdayLineup));
    }

    @Test
    void doesNotApplyDefensiveModifierToAnOffensiveLineup() {
        Lineup lineup = mock(Lineup.class);
        when(lineup.isDefensive()).thenReturn(false);

        assertEquals(0, new TeamMatchStats().calculateModBonus(
                new MatchdayLineup(lineup, List.of())));
    }

    @Test
    void acceptsAnIncompleteEffectiveLineupAndCountsASubstitute() {
        Lineup lineup = mock(Lineup.class);
        when(lineup.isDefensive()).thenReturn(false);

        PlayerMatchStats goalkeeper = player("P", 6.0);
        when(goalkeeper.getLineupPlayer().isStarter()).thenReturn(false);
        when(goalkeeper.calculateFantaRating()).thenReturn(6.0);
        PlayerMatchStats defender = player("D", 6.0);
        when(defender.calculateFantaRating()).thenReturn(6.0);

        MatchdayLineup effectiveLineup = new MatchdayLineup(
                lineup,
                List.of(goalkeeper, defender));

        assertDoesNotThrow(() -> new TeamMatchStats().calculateFantaRatingLineup(effectiveLineup));
        assertEquals(12.0, new TeamMatchStats().calculateFantaRatingLineup(effectiveLineup));
    }

    private static PlayerMatchStats player(String position, double vote) {
        LineupPlayer lineupPlayer = mock(LineupPlayer.class);
        when(lineupPlayer.isStarter()).thenReturn(true);
        when(lineupPlayer.getPosition()).thenReturn(org.generation.italy.fantafootball.model.entities.PlayerRole.valueOf(position));

        PlayerMatchStats stats = mock(PlayerMatchStats.class);
        when(stats.getLineupPlayer()).thenReturn(lineupPlayer);
        when(stats.getVote()).thenReturn(vote);
        return stats;
    }
}
