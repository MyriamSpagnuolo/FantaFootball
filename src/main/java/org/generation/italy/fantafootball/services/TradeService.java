package org.generation.italy.fantafootball.services;

import jakarta.transaction.Transactional;
import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.generation.italy.fantafootball.model.repositories.TradeRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TradeService {
    private final TradeRepository tradeRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    public TradeService(TradeRepository tradeRepository, TeamPlayerRepository teamPlayerRepository) {
        this.tradeRepository = tradeRepository;
        this.teamPlayerRepository = teamPlayerRepository;
    }

    public List<TradeDto> getAllByUserId(Long userId) {
        return toDtos(tradeRepository.findAllByUserId(userId));
    }

    public List<TradeDto> getAllPendingSentTradesByTeamId(Long teamId) {
        return toDtos(tradeRepository.findByProposingTeam_IdAndStatus(teamId, TradeStatus.PENDING));
    }

    public List<TradeDto> getAllPendingReceivedTradesByTeamId(Long teamId) {
        return toDtos(tradeRepository.findByReceivingTeam_IdAndStatus(teamId, TradeStatus.PENDING));
    }

    public List<TradeDto> getTradeHistoryByTeamId(Long teamId) {
        return toDtos(tradeRepository.findTradeHistoryByTeamId(teamId));
    }

    public void rejectTradeById(Long tradeId, Long userId) {
        Trade trade = findTrade(tradeId);
        validateParticipant(trade, userId);
        validatePending(trade, "rejected");

        trade.setStatus(TradeStatus.REJECTED);
        tradeRepository.save(trade);
    }

    @Transactional
    public void acceptTradeById(Long tradeId, Long userId) {
        Trade trade = findTrade(tradeId);
        validateReceiver(trade, userId);
        validatePending(trade, "accepted");
        validatePlayersStillAvailable(trade);
        settleAmount(trade);
        transferPlayers(trade);

        trade.setStatus(TradeStatus.ACCEPTED);
        tradeRepository.save(trade);
    }

    private Trade findTrade(Long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));
    }

    private void validateParticipant(Trade trade, Long userId) {
        boolean participant = Objects.equals(trade.getProposingTeam().getUser().getId(), userId)
                || Objects.equals(trade.getReceivingTeam().getUser().getId(), userId);
        if (!participant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to modify this trade");
        }
    }

    private void validateReceiver(Trade trade, Long userId) {
        if (!Objects.equals(trade.getReceivingTeam().getUser().getId(), userId)) {
            throw new AccessDeniedException("Only the receiving team owner can accept this trade");
        }
    }

    private void validatePending(Trade trade, String operation) {
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only pending trade requests can be " + operation);
        }
    }

    private void validatePlayersStillAvailable(Trade trade) {
        TeamPlayer requestedPlayer = trade.getRequestedPlayer();
        TeamPlayer offeredPlayer = trade.getOfferedPlayer();
        boolean available = requestedPlayer.getTransferDate() == null
                && offeredPlayer.getTransferDate() == null
                && Objects.equals(requestedPlayer.getTeam().getId(), trade.getReceivingTeam().getId())
                && Objects.equals(offeredPlayer.getTeam().getId(), trade.getProposingTeam().getId());
        if (!available) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The players in this trade are no longer available");
        }
    }

    /** Positive amount is paid by the proposing team; negative by the receiving team. */
    private void settleAmount(Trade trade) {
        long amount = trade.getAmount() == null ? 0L : trade.getAmount();
        Team proposingTeam = trade.getProposingTeam();
        Team receivingTeam = trade.getReceivingTeam();
        Team payer = amount >= 0 ? proposingTeam : receivingTeam;
        long payment = Math.abs(amount);

        if (payment > payer.getBudget()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient budget to accept this trade");
        }

        proposingTeam.setBudget(Math.toIntExact(proposingTeam.getBudget() - amount));
        receivingTeam.setBudget(Math.toIntExact(receivingTeam.getBudget() + amount));
    }

    private void transferPlayers(Trade trade) {
        LocalDate transferDate = LocalDate.now();
        TeamPlayer requestedPlayer = trade.getRequestedPlayer();
        TeamPlayer offeredPlayer = trade.getOfferedPlayer();
        requestedPlayer.setTransferDate(transferDate);
        offeredPlayer.setTransferDate(transferDate);
        teamPlayerRepository.saveAllAndFlush(List.of(requestedPlayer, offeredPlayer));
        teamPlayerRepository.save(new TeamPlayer(trade.getProposingTeam(), trade.getProposingTeam().getLeague(),
                requestedPlayer.getPlayer(), transferDate, requestedPlayer.getPurchasePrice()));
        teamPlayerRepository.save(new TeamPlayer(trade.getReceivingTeam(), trade.getReceivingTeam().getLeague(),
                offeredPlayer.getPlayer(), transferDate, offeredPlayer.getPurchasePrice()));
    }

    private List<TradeDto> toDtos(List<Trade> trades) {
        return trades.stream().map(TradeDto::fromEntity).toList();
    }

}
