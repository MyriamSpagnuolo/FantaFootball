package org.generation.italy.fantafootball.services;

import org.aspectj.weaver.ast.Not;
import org.generation.italy.fantafootball.model.dto.PurchasePlayerRequest;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalAuctionImportServiceTest {

    @Mock
    TeamRepository teamRepo;
    @Mock
    TeamPlayerRepository teamPlayerRepo;
    @Mock
    LeagueRepository leagueRepo;
    @Mock
    PlayerRepository playerRepo;
    @InjectMocks
    ExternalAuctionImportService service;

    private Long teamId;
    private Long userId;
    private Long playerId;
    private PurchasePlayerRequest purchasePlayerRequest;
    private Long leagueId;
    private Long nullId;
    private Long invalidId;


    @BeforeEach
    void setUp() {
        nullId = null;
        invalidId = -3L;
        leagueId = 1L;
        teamId = 1L;
        userId = 1L;
        playerId = 1L;
        purchasePlayerRequest = new PurchasePlayerRequest(10);
    }

    @Test
    void LEAGUE_ID_NULL_SHOULD_THROW_BAD_REQUEST_EXCEPTION() {
        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> service.importPlayer(
                        nullId,
                        teamId,
                        userId,
                        playerId,
                        purchasePlayerRequest
                )
        );

        assertEquals("invalid_league_id", e.getErrorCode());
        verifyNoInteractions(teamRepo, teamPlayerRepo, leagueRepo, playerRepo);
    }

    @Test
    void LEAGUE_ID_NOT_VALID_SHOULD_THROW_CONFLICT_EXCEPTION() {
         NotFoundException e = assertThrows(
                 NotFoundException.class,
                 ()-> service.importPlayer(
                         invalidId,
                         teamId,
                         userId,
                         playerId,
                         purchasePlayerRequest)
         );

         assertEquals("league_not_found",e.getErrorCode());
    }

    @Test
    void TEAM_ID_NULL_SHOULD_THROW_BAD_REQUEST_EXCEPTION() {
        League league = mock(League.class);
        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> service.importPlayer(
                        leagueId,
                        nullId,
                        userId,
                        playerId,
                        purchasePlayerRequest)
        );
        assertEquals("invalid_team_id",e.getErrorCode());
        verifyNoInteractions(teamRepo,playerRepo);
    }
    @Test
    void TEAM_NOT_FOUND_SHOULD_THROW_NOT_FOUND_EXCEPTION() {
        League league = mock(League.class);
        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));

        NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> service.importPlayer(
                        leagueId,
                        invalidId,
                        playerId,
                        userId,
                        purchasePlayerRequest
                )
        );
        assertEquals("team_not_found",e.getErrorCode());
    }
    @Test
    void PLAYER_ID_NULL_SHOULD_THROW_BAD_REQUEST_EXCEPTION() {
        League league = mock(League.class);
        Team team = mock(Team.class);
        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));
        when(teamRepo.findById(userId)).thenReturn(Optional.of(team));

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> service.importPlayer(
                        leagueId,
                        teamId,
                        userId,
                        nullId,
                        purchasePlayerRequest
                )
        );
        assertEquals("invalid_player_id",e.getErrorCode());
        verifyNoInteractions(teamPlayerRepo,playerRepo);
    }

    @Test
    void PLAYER_ID_INVALID_SHOULD_THROW_NOT_FOUND_EXCEPTION() {
        League league = mock(League.class);
        Team team = mock(Team.class);
        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));
        when(teamRepo.findById(teamId)).thenReturn(Optional.of(team));

        NotFoundException e = assertThrows(
                NotFoundException.class,
                ()-> service.importPlayer(
                        leagueId,
                        teamId,
                        userId,
                        invalidId,
                        purchasePlayerRequest
                )
        );
        assertEquals("player_not_found",e.getErrorCode());
    }
    @Test
    void TEAM_NOT_BELONGING_TO_LEAGUE_SHOULD_THROW_BAD_REQUEST_EXCEPTION() {
        League league = mock(League.class);
        Team team = mock(Team.class);
        Player player = mock(Player.class);
        League diffLeague = mock(League.class);

        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));
        when(teamRepo.findById(teamId)).thenReturn(Optional.of(team));
        when(playerRepo.findById(playerId)).thenReturn(Optional.of(player));
        when(league.getId()).thenReturn(1L);
        when(diffLeague.getId()).thenReturn(2L);
        when(team.getLeague()).thenReturn(diffLeague);

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> service.importPlayer(
                        userId,
                        teamId,
                        playerId,
                        leagueId,
                        purchasePlayerRequest
                )
        );
        assertEquals("league_id_not_valid",e.getErrorCode());
        verifyNoInteractions(teamPlayerRepo);
    }

    @Test
    void USER_NOT_ADMIN_OF_THE_LEAGUE_SHOULD_THROW_CONFLICT_EXCEPTION() {
        League league = mock(League.class);
        Team team = mock(Team.class);
        Player player = mock(Player.class);
        AppUser user = mock(AppUser.class);

        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));
        when(teamRepo.findById(teamId)).thenReturn(Optional.of(team));
        when(playerRepo.findById(playerId)).thenReturn(Optional.of(player));
        when(league.getId()).thenReturn(1L);
        when(team.getLeague()).thenReturn(league);
        when(user.getId()).thenReturn(2L);
        when(league.getAdmin()).thenReturn(user);

        AccessDeniedException e = assertThrows(
                AccessDeniedException.class,
                ()->service.importPlayer(
                        userId,
                        playerId,
                        leagueId,
                        teamId,
                        purchasePlayerRequest
                )
        );
        assertEquals("Solo l'admin della lega può importare giocatori",e.getMessage());
        verifyNoInteractions(teamPlayerRepo);
    }

}
