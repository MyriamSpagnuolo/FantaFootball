package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.PurchasePlayerRequest;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @BeforeEach
    void setUp() {
        teamId = 1L;
        userId = 1L;
        playerId = 1L;
    }

    @Test
    void league_Null_Should_Throw_An_Exception() {
        Long leagueId = null;

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> service.importPlayer(
                        leagueId,
                        teamId,
                        userId,
                        playerId,
                        new PurchasePlayerRequest(10)
                )
        );

        assertEquals("invalid_league_id", e.getErrorCode());
        verifyNoInteractions(teamRepo, teamPlayerRepo, leagueRepo, playerRepo);
    }
}
