package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.generation.italy.fantafootball.model.repositories.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
    @Mock
    private LeagueRepository leagueRepository;

    private TradeService service;
    private Trade trade;
    private League league;
    private Team proposingTeam;
    private Team receivingTeam;
    private TeamPlayer requestedPlayer;
    private TeamPlayer offeredPlayer;

    @BeforeEach
    void setUp() {
        service = new TradeService(tradeRepository, teamPlayerRepository, teamRepository, leagueRepository);

        AppUser proposer = new AppUser("proposer", "hash", Set.of());
        AppUser receiver = new AppUser("receiver", "hash", Set.of());
        league = new League("League", "CODE", proposer);
        proposingTeam = new Team("Proposers", proposer, league);
        receivingTeam = new Team("Receivers", receiver, league);
        requestedPlayer = mock(TeamPlayer.class);
        offeredPlayer = mock(TeamPlayer.class);
        trade = mock(Trade.class);

        setId(proposingTeam, 1L);
        setId(receivingTeam, 2L);
        setId(proposer, 10L);
        setId(receiver, 20L);
        setId(league, 100L);

    }

    @Test
    void rejectsAcceptanceWhenReceivingTeamCannotPayNegativeAmount() {
        proposingTeam.setBudget(100);
        receivingTeam.setBudget(20);
        stubValidPendingTrade();
        when(trade.getAmount()).thenReturn(-21);

        assertThatThrownBy(() -> service.acceptTradeById(1L, 20L))
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((ConflictException) exception).getErrorCode())
                .isEqualTo("insufficient_budget");

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
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(teamRepository, teamPlayerRepository);
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void refusesReadingAnotherUsersTeamTrades() {
        when(teamRepository.findByIdAndUserId(2L, 999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllPendingReceivedTradesByTeamId(2L, 999L))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(tradeRepository);
    }

    @Test
    void returnsTradesForLeagueMember() {
        Player requestedPlayerEntity = new Player(1L, "Requested", "Player", "Team A", 7, 10, false, PlayerRole.A);
        Player offeredPlayerEntity = new Player(2L, "Offered", "Player", "Team B", 9, 15, false, PlayerRole.A);
        TeamPlayer realRequestedPlayer = new TeamPlayer(receivingTeam, league, requestedPlayerEntity,
                java.time.LocalDate.now(), 10);
        TeamPlayer realOfferedPlayer = new TeamPlayer(proposingTeam, league, offeredPlayerEntity,
                java.time.LocalDate.now(), 15);
        Trade realTrade = new Trade(proposingTeam, receivingTeam, realRequestedPlayer, realOfferedPlayer, TradeStatus.PENDING);
        realTrade.setProposalDate(java.time.LocalDateTime.now());
        setId(realTrade, 1L);

        when(leagueRepository.existsById(100L)).thenReturn(true);
        when(teamRepository.existsByUserIdAndLeagueId(10L, 100L)).thenReturn(true);
        when(tradeRepository.findAllByLeagueId(100L)).thenReturn(List.of(realTrade));

        List<TradeDto> result = service.getAllByLeagueId(100L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).proposingTeamId()).isEqualTo(proposingTeam.getId());
        assertThat(result.get(0).receivingTeamId()).isEqualTo(receivingTeam.getId());
        verify(tradeRepository).findAllByLeagueId(100L);
    }

    @Test
    void refusesReadingTradesForNonExistingLeague() {
        when(leagueRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.getAllByLeagueId(999L, 10L))
                .isInstanceOf(NotFoundException.class)
                .extracting(exception -> ((NotFoundException) exception).getErrorCode())
                .isEqualTo("league_not_found");

        verifyNoInteractions(tradeRepository);
    }

    @Test
    void refusesReadingLeagueTradesForNonMember() {
        when(leagueRepository.existsById(100L)).thenReturn(true);
        when(teamRepository.existsByUserIdAndLeagueId(999L, 100L)).thenReturn(false);

        assertThatThrownBy(() -> service.getAllByLeagueId(100L, 999L))
                .isInstanceOf(AccessDeniedException.class);

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
