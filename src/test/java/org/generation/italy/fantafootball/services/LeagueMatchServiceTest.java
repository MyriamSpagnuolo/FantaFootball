package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.algorithms.RoundRobinScheduler;
import org.generation.italy.fantafootball.algorithms.RoundRobinScheduler.MatchPairing;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LeagueMatchRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.MatchdayRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueMatchServiceTest {

    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private MatchdayRepository matchdayRepository;
    @Mock
    private LeagueMatchRepository leagueMatchRepository;
    @Mock
    private RoundRobinScheduler roundRobinScheduler;

    private LeagueMatchService leagueMatchService;

    private League league;
    private Team teamA;
    private Team teamB;
    private Team teamC;

    @BeforeEach
    void setUp() {
        leagueMatchService = new LeagueMatchService(
                leagueRepository, teamRepository, matchdayRepository, leagueMatchRepository, roundRobinScheduler
        );

        AppUser admin = new AppUser("admin", "hash", Set.of());
        league = new League("Lega di prova", "INVITE1", admin);

        teamA = new Team("Team A", admin, league);
        teamB = new Team("Team B", admin, league);
        teamC = new Team("Team C", admin, league);
    }

    @Test
    void throwsNotFoundWhenLeagueDoesNotExist() {
        when(leagueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leagueMatchService.generateCalendar(1L))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(teamRepository, matchdayRepository, roundRobinScheduler);
        verify(leagueMatchRepository, never()).saveAll(any());
    }

    @Test
    void throwsConflictWhenCalendarAlreadyGenerated() {
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));
        when(leagueMatchRepository.existsByLeagueId(1L)).thenReturn(true);

        assertThatThrownBy(() -> leagueMatchService.generateCalendar(1L))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(teamRepository, matchdayRepository, roundRobinScheduler);
        verify(leagueMatchRepository, never()).saveAll(any());
    }

    @Test
    void buildsOneLeagueMatchPerPairingBoundToTheMatchingRoundMatchday() {
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));
        when(leagueMatchRepository.existsByLeagueId(1L)).thenReturn(false);

        List<Team> teams = List.of(teamA, teamB, teamC);
        when(teamRepository.findAllTeamByLeagueId(1L)).thenReturn(teams);

        Matchday matchday1 = new Matchday(1, LocalDate.of(2026, 9, 1));
        Matchday matchday2 = new Matchday(2, LocalDate.of(2026, 9, 8));
        List<Matchday> matchdays = List.of(matchday1, matchday2);
        when(matchdayRepository.findByClosedFalseOrderByDateAsc()).thenReturn(matchdays);

        List<List<MatchPairing>> schedule = List.of(
                List.of(new MatchPairing(teamA, teamB)),
                List.of(new MatchPairing(teamB, teamC), new MatchPairing(teamC, teamA))
        );
        when(roundRobinScheduler.generateSchedule(teams, 2)).thenReturn(schedule);

        when(leagueMatchRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<LeagueMatch> result = leagueMatchService.generateCalendar(1L);

        ArgumentCaptor<List<LeagueMatch>> captor = ArgumentCaptor.forClass(List.class);
        verify(leagueMatchRepository).saveAll(captor.capture());
        List<LeagueMatch> saved = captor.getValue();

        assertThat(saved).hasSize(3);
        assertThat(result).isEqualTo(saved);

        LeagueMatch firstRoundMatch = saved.get(0);
        assertThat(firstRoundMatch.getLeague()).isEqualTo(league);
        assertThat(firstRoundMatch.getHomeTeam()).isEqualTo(teamA);
        assertThat(firstRoundMatch.getAwayTeam()).isEqualTo(teamB);
        assertThat(firstRoundMatch.getMatchday()).isEqualTo(matchday1);
        assertThat(firstRoundMatch.getRoundNumber()).isEqualTo(1);
        assertThat(firstRoundMatch.getMatchDay()).isEqualTo(matchday1.getDate().atStartOfDay());

        LeagueMatch secondRoundFirstMatch = saved.get(1);
        LeagueMatch secondRoundSecondMatch = saved.get(2);
        assertThat(secondRoundFirstMatch.getMatchday()).isEqualTo(matchday2);
        assertThat(secondRoundFirstMatch.getRoundNumber()).isEqualTo(2);
        assertThat(secondRoundSecondMatch.getMatchday()).isEqualTo(matchday2);
        assertThat(secondRoundSecondMatch.getRoundNumber()).isEqualTo(2);
    }
}
