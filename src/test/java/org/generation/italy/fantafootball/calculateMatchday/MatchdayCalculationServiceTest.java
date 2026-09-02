package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LineupRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchdayCalculationServiceTest {

    @Mock
    LineupRepository lineupRepository;
    @Mock
    PlayerResultRepository playerResultRepository;
    @InjectMocks
    MatchdayCalculationService calculationService;

    @Test
    void calculateLineupScoreRejectsUnknownLineup() {
        when(lineupRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> calculationService.calculateLineupScore(77L));
    }

    @Test
    void calculateLineupScoreRejectsOpenMatchday() {
        Team home = team(1L);
        Team away = team(2L);
        Matchday matchday = matchday(5L, false);
        LeagueMatch match = match(home, away, matchday);
        Lineup lineup = new Lineup(home, match, null, false);

        when(lineupRepository.findById(10L)).thenReturn(Optional.of(lineup));

        assertThrows(IllegalStateException.class, () -> calculationService.calculateLineupScore(10L));
    }

    @Test
    void calculateLineupScoreUsesSameRoleSubstituteWhenStarterDidNotPlay() {
        Team home = team(1L);
        Team away = team(2L);
        Matchday matchday = matchday(5L, true);
        LeagueMatch match = match(home, away, matchday);
        Lineup lineup = new Lineup(home, match, null, false);
        LineupPlayer starter = lineupPlayer(lineup, teamPlayer(11L, 101L, PlayerRole.D), true);
        LineupPlayer substitute = lineupPlayer(lineup, teamPlayer(12L, 102L, PlayerRole.D), false);
        lineup.addPlayer(starter);
        lineup.addPlayer(substitute);

        when(lineupRepository.findById(10L)).thenReturn(Optional.of(lineup));
        when(playerResultRepository.findByPlayerIdAndMatchdayId(101L, 5L))
                .thenReturn(Optional.of(playerResult(null)));
        when(playerResultRepository.findByPlayerIdAndMatchdayId(102L, 5L))
                .thenReturn(Optional.of(playerResult("6.5")));

        assertEquals(6.5, calculationService.calculateLineupScore(10L));
    }

    private static LeagueMatch match(Team home, Team away, Matchday matchday) {
        LeagueMatch match = new LeagueMatch();
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setMatchday(matchday);
        return match;
    }

    private static Matchday matchday(Long id, boolean closed) {
        Matchday matchday = new Matchday(1, LocalDate.now());
        setId(matchday, id);
        matchday.setClosed(closed);
        return matchday;
    }

    private static Team team(Long id) {
        Team team = new Team();
        setId(team, id);
        return team;
    }

    private static TeamPlayer teamPlayer(Long teamPlayerId, Long playerId, PlayerRole role) {
        Player player = new Player(playerId, "Name" + playerId, "Surname", "Team", 1, 1, false, role);
        setId(player, playerId);

        TeamPlayer teamPlayer = new TeamPlayer();
        setId(teamPlayer, teamPlayerId);
        teamPlayer.setPlayer(player);
        return teamPlayer;
    }

    private static LineupPlayer lineupPlayer(Lineup lineup, TeamPlayer teamPlayer, boolean starter) {
        LineupPlayer lineupPlayer = new LineupPlayer();
        lineupPlayer.setLineup(lineup);
        lineupPlayer.setTeamPlayer(teamPlayer);
        lineupPlayer.setStarter(starter);
        return lineupPlayer;
    }

    private static PlayerResult playerResult(String rating) {
        PlayerResult result = new PlayerResult();
        result.setRating(rating == null ? null : new BigDecimal(rating));
        return result;
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
