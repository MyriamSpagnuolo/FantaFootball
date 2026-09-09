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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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
                () -> playerService.findPlayers(new PlayerFilterRequest(null, null, 20, 10, null), 0, 20)
        );

        assertEquals("INVALID_PRICE_RANGE", exception.getErrorCode());
    }

    @Test
    void findPlayersRejectsBlankRealTeamName() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> playerService.findPlayers(new PlayerFilterRequest(null, "  ", null, null, null), 0, 20)
        );

        assertEquals("INVALID_REAL_TEAM", exception.getErrorCode());
    }

    @Test
    void findPlayersAppliesFiltersAndMapsResponses() {
        Player player = new Player(99L, "Mario", "Rossi", "Inter", 10, 25, false, PlayerRole.A);
        when(playerRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Player>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(player), PlayerPagination.of(1, 1), 3));

        var result = playerService.findPlayers(new PlayerFilterRequest(PlayerRole.A, " Inter ", 10, 30, false), 1, 1);

        ArgumentCaptor<Specification<Player>> captor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pagination = ArgumentCaptor.forClass(Pageable.class);
        verify(playerRepository).findAll(captor.capture(), pagination.capture());
        assertEquals(PlayerPagination.of(1, 1), pagination.getValue());
        assertEquals(1, result.content().size());
        assertEquals("Mario", result.content().getFirst().name());
        assertEquals(PlayerRole.A, result.content().getFirst().role());
        assertEquals(1, result.page());
        assertEquals(3, result.totalElements());
    }
}
