package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.services.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    @Mock
    private TradeService tradeService;
    @Mock
    private Jwt jwt;

    private TradeController controller;

    @BeforeEach
    void setUp() {
        controller = new TradeController(tradeService);
    }

    @Test
    void passesAuthenticatedUserWhenLoadingReceivedTrades() {
        when(jwt.getClaim("uid")).thenReturn(42L);
        when(tradeService.getAllPendingReceivedTradesByTeamId(7L, 42L)).thenReturn(List.of());

        controller.getPendingReceivedTradeRequests(7L, jwt);

        verify(tradeService).getAllPendingReceivedTradesByTeamId(7L, 42L);
    }

    @Test
    void rejectsTokenWithoutNumericUserId() {
        when(jwt.getClaim("uid")).thenReturn("42");

        assertThatThrownBy(() -> controller.getAllTradesByUserId(jwt))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(tradeService);
    }

    @Test
    void acceptsTradeUsingAuthenticatedUser() {
        when(jwt.getClaim("uid")).thenReturn(42L);

        controller.acceptTradeRequestById(5L, jwt);

        verify(tradeService).acceptTradeById(5L, 42L);
    }
}
