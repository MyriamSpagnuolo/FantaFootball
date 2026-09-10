package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.repositories.LeagueMatchRepository;
import org.generation.italy.fantafootball.model.repositories.LineupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueMatchScoreServiceTest {

    @Mock
    private LeagueMatchRepository leagueMatchRepository;
    @Mock
    private LineupRepository lineupRepository;
    @Mock
    private MatchdayCalculationService matchdayCalculationService;

    // Standing e' logica pura senza dipendenze (vedi StandingTest): usiamo l'istanza vera per
    // verificare anche il suo effetto reale (score/gol/punti), non solo che venga chiamata.
    private final Standing standing = new Standing();

    private LeagueMatchScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new LeagueMatchScoreService(
                leagueMatchRepository, lineupRepository, matchdayCalculationService, standing);
    }

    @Test
    void savesScoreGoalsAndStandingPointsForEachMatchOfTheMatchday() {
        Team home = team(1L);
        Team away = team(2L);
        LeagueMatch match = match(10L, home, away);
        when(leagueMatchRepository.findAllByMatchdayIdAndHomeGoalsIsNull(5L)).thenReturn(List.of(match));

        Lineup homeLineup = lineup(100L);
        Lineup awayLineup = lineup(200L);
        when(lineupRepository.findByTeamIdAndLeagueMatchId(1L, 10L)).thenReturn(Optional.of(homeLineup));
        when(lineupRepository.findByTeamIdAndLeagueMatchId(2L, 10L)).thenReturn(Optional.of(awayLineup));
        // 72 punti -> 2 gol, 60 punti -> 0 gol (vedi GoalsCalculator: soglia 67, poi un gol ogni 5).
        when(matchdayCalculationService.calculateLineupScore(100L)).thenReturn(72.0);
        when(matchdayCalculationService.calculateLineupScore(200L)).thenReturn(60.0);

        scoreService.calculateAndSaveResultsForMatchday(5L);

        assertEquals(0, BigDecimal.valueOf(72.0).compareTo(match.getHomeScore()));
        assertEquals(0, BigDecimal.valueOf(60.0).compareTo(match.getAwayScore()));
        assertEquals(2, match.getHomeGoals());
        assertEquals(0, match.getAwayGoals());
        assertEquals(3, home.getTotalPoints());
        assertEquals(0, away.getTotalPoints());
        verify(leagueMatchRepository).save(match);
    }

    @Test
    void teamWithoutASubmittedLineupScoresZeroInsteadOfFailing() {
        Team home = team(1L);
        Team away = team(2L);
        LeagueMatch match = match(10L, home, away);
        when(leagueMatchRepository.findAllByMatchdayIdAndHomeGoalsIsNull(5L)).thenReturn(List.of(match));
        when(lineupRepository.findByTeamIdAndLeagueMatchId(any(), any())).thenReturn(Optional.empty());

        scoreService.calculateAndSaveResultsForMatchday(5L);

        assertEquals(0, BigDecimal.ZERO.compareTo(match.getHomeScore()));
        assertEquals(0, BigDecimal.ZERO.compareTo(match.getAwayScore()));
        // Nessuno dei due ha schierato una formazione: 0-0, pareggio per entrambi.
        assertEquals(1, home.getTotalPoints());
        assertEquals(1, away.getTotalPoints());
    }

    private static Team team(Long id) {
        Team team = new Team();
        setId(team, id);
        return team;
    }

    private static LeagueMatch match(Long id, Team home, Team away) {
        LeagueMatch match = new LeagueMatch();
        setId(match, id);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        return match;
    }

    private static Lineup lineup(Long id) {
        Lineup lineup = new Lineup();
        setId(lineup, id);
        return lineup;
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
