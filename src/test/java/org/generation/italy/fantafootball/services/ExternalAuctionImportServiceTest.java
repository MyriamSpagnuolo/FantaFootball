package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.AuctionPlayerImportRequest;
import org.generation.italy.fantafootball.model.dto.AuctionRosterImportRequest;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
@ExtendWith(MockitoExtension.class)
class ExternalAuctionImportServiceTest {

    @Mock
    TeamRepository teamRepo;
    @Mock
    TeamPlayerRepository teamPlayerRepo;
    @Mock
    LeagueRepository leagueRepo;
    @InjectMocks
    ExternalAuctionImportService service;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void league_Null_Should_Throw_An_Exception() {
        Long leagueId = null;
        Long teamId = 1L;
        Long userid = 1L;
        AuctionRosterImportRequest rost = null;

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () ->service.importRoster(null,teamId,userid,rost)
        );

        assertEquals("invalid_league_id",e.getErrorCode());
        verifyNoInteractions(teamRepo, teamPlayerRepo, leagueRepo);


    }

    @Test
    void importRoster() {
    }
}