package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.PlayerFilterRequest;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    PlayerRepository playerRepository;
    @InjectMocks
    PlayerService playerService;

    @Test
    void findPlayersRejectsInvalidPriceRange() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> playerService.findPlayers(new PlayerFilterRequest(null, null, 20, 10, null))
        );

        assertEquals("INVALID_PRICE_RANGE", exception.getErrorCode());
    }

    @Test
    void findPlayersRejectsBlankRealTeamName() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> playerService.findPlayers(new PlayerFilterRequest(null, "  ", null, null, null))
        );

        assertEquals("INVALID_REAL_TEAM", exception.getErrorCode());
    }

    @Test
    void findPlayersAppliesFiltersAndMapsResponses() {
        Player player = new Player(99L, "Mario", "Rossi", "Inter", 10, 25, false, PlayerRole.A);
        when(playerRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Player>>any()))
                .thenReturn(List.of(player));

        var result = playerService.findPlayers(new PlayerFilterRequest(PlayerRole.A, " Inter ", 10, 30, false));

        ArgumentCaptor<Specification<Player>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(playerRepository).findAll(captor.capture());
        assertEquals(1, result.size());
        assertEquals("Mario", result.getFirst().name());
        assertEquals(PlayerRole.A, result.getFirst().role());
    }
}
