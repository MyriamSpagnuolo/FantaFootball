package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.CreateTeamRequest;
import org.generation.italy.fantafootball.model.dto.RenameTeamRequest;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.LeagueInvite;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueInviteRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    TeamRepository teamRepository;
    @Mock
    TeamPlayerRepository teamPlayerRepository;
    @Mock
    AppUserRepository appUserRepository;
    @Mock
    LeagueRepository leagueRepository;
    @Mock
    LeagueInviteRepository leagueInviteRepository;
    @InjectMocks
    TeamService teamService;

    @Mock
    PlayerResultRepository playerResultRepository;

    @Test
    void rosterCalculatesSeparateAveragesAndExcludesInvalidResults() {
        Team team = new Team();
        setId(team, 10L);
        TeamPlayer first = rosterPlayer(team, 1L);
        TeamPlayer second = rosterPlayer(team, 2L);
        TeamPlayer withoutVotes = rosterPlayer(team, 3L);
        TeamPlayer withoutResults = rosterPlayer(team, 4L);
        PlayerResult goal = result(first.getPlayer(), "6", true);
        goal.setGoalNum(1);
        PlayerResult yellowCard = result(second.getPlayer(), "7", true);
        yellowCard.setYellowCard(1);
        when(teamRepository.existsById(10L)).thenReturn(true);
        when(teamPlayerRepository.findAllByTeamId(10L))
                .thenReturn(List.of(first, second, withoutVotes, withoutResults));
        when(playerResultRepository.findAllByPlayerIdIn(List.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(goal, result(first.getPlayer(), "5", true),
                        result(first.getPlayer(), "10", false), yellowCard,
                        result(withoutVotes.getPlayer(), null, true)));

        var roster = teamService.getTeamRoster(10L);

        assertEquals(4, roster.size());
        assertEquals(1L, roster.get(0).playerId());
        assertEquals(7.0, roster.get(0).fantaAverage());
        assertEquals(2L, roster.get(1).playerId());
        assertEquals(6.5, roster.get(1).fantaAverage());
        org.junit.jupiter.api.Assertions.assertNull(roster.get(2).fantaAverage());
        org.junit.jupiter.api.Assertions.assertNull(roster.get(3).fantaAverage());
        verify(playerResultRepository).findAllByPlayerIdIn(List.of(1L, 2L, 3L, 4L));
    }

    @Test
    void emptyRosterDoesNotLoadResults() {
        when(teamRepository.existsById(10L)).thenReturn(true);
        when(teamPlayerRepository.findAllByTeamId(10L)).thenReturn(List.of());

        assertEquals(List.of(), teamService.getTeamRoster(10L));

        org.mockito.Mockito.verifyNoInteractions(playerResultRepository);
    }

    private static TeamPlayer rosterPlayer(Team team, Long playerId) {
        Player player = new Player();
        setId(player, playerId);
        player.setRole(PlayerRole.A);
        TeamPlayer teamPlayer = new TeamPlayer();
        setId(teamPlayer, playerId + 100);
        teamPlayer.setTeam(team);
        teamPlayer.setPlayer(player);
        return teamPlayer;
    }

    private static PlayerResult result(Player player, String rating, boolean closed) {
        Matchday matchday = new Matchday();
        matchday.setClosed(closed);
        PlayerResult result = new PlayerResult();
        result.setPlayer(player);
        result.setMatchday(matchday);
        result.setRating(rating == null ? null : new BigDecimal(rating));
        return result;
    }

    @Test
    void createTeamAllowsLeagueAdminAndCopiesLeagueBudget() {
        AppUser admin = user(7L);
        League league = league(3L, admin, 500);

        when(appUserRepository.findById(7L)).thenReturn(Optional.of(admin));
        when(teamRepository.existsByNameAndLeagueId("My Team", 3L)).thenReturn(false);
        when(teamRepository.existsByUserIdAndLeagueId(7L, 3L)).thenReturn(false);
        when(leagueRepository.findById(3L)).thenReturn(Optional.of(league));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        teamService.createTeam(new CreateTeamRequest("My Team", 3L), 7L);

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        assertEquals("My Team", captor.getValue().getName());
        assertEquals(500, captor.getValue().getBudget());
        assertEquals(admin, captor.getValue().getUser());
        assertEquals(league, captor.getValue().getLeague());
    }

    @Test
    void createTeamRequiresAcceptedInviteForNonAdmin() {
        AppUser admin = user(1L);
        AppUser invited = user(2L);
        League league = league(4L, admin, 300);
        LeagueInvite pendingInvite = new LeagueInvite(league, admin, invited, LeagueInviteStatus.PENDING);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(invited));
        when(teamRepository.existsByNameAndLeagueId("Guest Team", 4L)).thenReturn(false);
        when(teamRepository.existsByUserIdAndLeagueId(2L, 4L)).thenReturn(false);
        when(leagueRepository.findById(4L)).thenReturn(Optional.of(league));
        when(leagueInviteRepository.findTopByLeagueIdAndInvitedUserIdOrderBySentDateDesc(4L, 2L))
                .thenReturn(Optional.of(pendingInvite));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> teamService.createTeam(new CreateTeamRequest("Guest Team", 4L), 2L)
        );

        assertEquals("INVITE_PENDING", exception.getErrorCode());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void renameTeamRejectsNonOwner() {
        AppUser owner = user(8L);
        League league = league(5L, owner, 250);
        Team team = new Team("Old", owner, league);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        assertThrows(
                AccessDeniedException.class,
                () -> teamService.renameTeam(10L, 99L, new RenameTeamRequest("New"))
        );

        verify(teamRepository, never()).save(any());
    }

    private static AppUser user(Long id) {
        AppUser user = new AppUser();
        setId(user, id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@test.local");
        return user;
    }

    private static League league(Long id, AppUser admin, int budget) {
        League league = new League("League", "CODE" + id, admin);
        setId(league, id);
        league.setBudget(budget);
        return league;
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
