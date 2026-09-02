package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.generation.italy.fantafootball.model.repositories.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private TeamPlayerRepository teamPlayerRepository;
    @Mock
    private TeamRepository teamRepository;

    private TradeService service;
    private Trade trade;
    private Team proposingTeam;
    private Team receivingTeam;
    private TeamPlayer requestedPlayer;
    private TeamPlayer offeredPlayer;

    @BeforeEach
    void setUp() {
        service = new TradeService(tradeRepository, teamPlayerRepository, teamRepository);

        AppUser proposer = new AppUser("proposer", "hash", Set.of());
        AppUser receiver = new AppUser("receiver", "hash", Set.of());
        League league = new League("League", "CODE", proposer);
        proposingTeam = new Team("Proposers", proposer, league);
        receivingTeam = new Team("Receivers", receiver, league);
        requestedPlayer = mock(TeamPlayer.class);
        offeredPlayer = mock(TeamPlayer.class);
        trade = mock(Trade.class);

        setId(proposingTeam, 1L);
        setId(receivingTeam, 2L);
        setId(proposer, 10L);
        setId(receiver, 20L);

    }

    @Test
    void rejectsAcceptanceWhenReceivingTeamCannotPayNegativeAmount() {
        proposingTeam.setBudget(100);
        receivingTeam.setBudget(20);
        stubValidPendingTrade();
        when(trade.getAmount()).thenReturn(-21);

        assertThatThrownBy(() -> service.acceptTradeById(1L, 20L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(proposingTeam.getBudget()).isEqualTo(100);
        assertThat(receivingTeam.getBudget()).isEqualTo(20);
        verifyNoInteractions(teamPlayerRepository);
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void acceptsTradeAndSettlesPositiveAmount() {
        proposingTeam.setBudget(100);
        receivingTeam.setBudget(20);
        stubValidPendingTrade();
        when(trade.getAmount()).thenReturn(30);
        when(requestedPlayer.getId()).thenReturn(100L);
        when(offeredPlayer.getId()).thenReturn(200L);

        service.acceptTradeById(1L, 20L);

        assertThat(proposingTeam.getBudget()).isEqualTo(70);
        assertThat(receivingTeam.getBudget()).isEqualTo(50);
        verify(teamPlayerRepository).saveAllAndFlush(any());
        verify(teamPlayerRepository, times(2)).save(any(TeamPlayer.class));
        verify(trade).setStatus(TradeStatus.ACCEPTED);
        verify(tradeRepository).save(trade);
        verify(tradeRepository).cancelPendingTradesInvolvingPlayers(List.of(100L, 200L), 1L);
    }

    @Test
    void refusesAcceptanceByUserWhoDoesNotOwnReceivingTeam() {
        when(tradeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(trade));
        when(trade.getReceivingTeam()).thenReturn(receivingTeam);

        assertThatThrownBy(() -> service.acceptTradeById(1L, 999L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verifyNoInteractions(teamRepository, teamPlayerRepository);
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void refusesReadingAnotherUsersTeamTrades() {
        when(teamRepository.findByIdAndUserId(2L, 999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllPendingReceivedTradesByTeamId(2L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verifyNoInteractions(tradeRepository);
    }

    private static void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not configure test entity", exception);
        }
    }

    private void stubValidPendingTrade() {
        when(trade.getProposingTeam()).thenReturn(proposingTeam);
        when(trade.getReceivingTeam()).thenReturn(receivingTeam);
        when(trade.getRequestedPlayer()).thenReturn(requestedPlayer);
        when(trade.getOfferedPlayer()).thenReturn(offeredPlayer);
        when(trade.getStatus()).thenReturn(TradeStatus.PENDING);
        when(requestedPlayer.getTransferDate()).thenReturn(null);
        when(offeredPlayer.getTransferDate()).thenReturn(null);
        when(requestedPlayer.getTeam()).thenReturn(receivingTeam);
        when(offeredPlayer.getTeam()).thenReturn(proposingTeam);
        when(tradeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(trade));
        when(teamRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(proposingTeam));
        when(teamRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(receivingTeam));
    }
}
