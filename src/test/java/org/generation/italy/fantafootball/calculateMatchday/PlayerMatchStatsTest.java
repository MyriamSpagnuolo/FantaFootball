package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PlayerMatchStatsTest {
    @Test
    void calculatesAllPlayerBonusesAndMaluses() {
        LineupPlayer lineupPlayer = mock(LineupPlayer.class);
        PlayerResult result = mock(PlayerResult.class);
        when(lineupPlayer.getTeamPlayer()).thenReturn(mock());
        when(result.getRating()).thenReturn(new BigDecimal("6.5"));
        when(result.getGoalNum()).thenReturn(1);
        when(result.getAutogoalNum()).thenReturn(0);
        when(result.getAssistNum()).thenReturn(1);
        when(result.getPenaltySaved()).thenReturn(1);
        when(result.getPenaltyFailed()).thenReturn(0);
        when(result.getYellowCard()).thenReturn(1);
        when(result.isRedCard()).thenReturn(false);
        when(result.getCleanSheet()).thenReturn(true);

        assertEquals(12.0, new PlayerMatchStats(lineupPlayer, result).calculateFantaRating());
    }
}
