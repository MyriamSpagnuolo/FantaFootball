package org.generation.italy.fantafootball.services;

import jakarta.transaction.Transactional;
import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
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

    public TradeService(TradeRepository tradeRepository, TeamPlayerRepository teamPlayerRepository){
        this.tradeRepository = tradeRepository;
        this.teamPlayerRepository = teamPlayerRepository;
    }

    public List<TradeDto> getAllByUserId(Long id){
        return tradeRepository.findAllByUserId(id).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public List<TradeDto> getAllPendingSentTradesByTeamId(Long id){
        return tradeRepository.findByProposingTeam_IdAndStatus(id, TradeStatus.PENDING).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public List<TradeDto> getAllPendingReceivedTradesByTeamId(Long id){
        return tradeRepository.findByReceivingTeam_IdAndStatus(id, TradeStatus.PENDING).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public List<TradeDto> getTradeHistoryByTeamId(Long id){
        return tradeRepository.findTradeHistoryByTeamId(id).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public void rejectTradeById(Long id, Long userId){
        var trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));

        boolean belongsToUser = trade.getProposingTeam().getUser().getId().equals(userId)
                || trade.getReceivingTeam().getUser().getId().equals(userId);

        if (!belongsToUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to delete this trade");
        }

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only pending trade requests can be rejected");
        }

        trade.setStatus(TradeStatus.REJECTED);
        tradeRepository.save(trade);
    }

    @Transactional
    public void acceptTradeById(Long id, Long userId) {
        var trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));

        if (!Objects.equals(trade.getReceivingTeam().getUser().getId(), userId)) {
            throw new AccessDeniedException("Only the receiving team owner can accept this trade");
        }

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only pending trade requests can be accepted");
        }

        TeamPlayer requestedPlayer = trade.getRequestedPlayer();
        TeamPlayer offeredPlayer = trade.getOfferedPlayer();

        if (requestedPlayer.getTransferDate() != null || offeredPlayer.getTransferDate() != null
                || !Objects.equals(requestedPlayer.getTeam().getId(), trade.getReceivingTeam().getId())
                || !Objects.equals(offeredPlayer.getTeam().getId(), trade.getProposingTeam().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The players in this trade are no longer available");
        }

        LocalDate transferDate = LocalDate.now();
        requestedPlayer.setTransferDate(transferDate);
        offeredPlayer.setTransferDate(transferDate);
        teamPlayerRepository.saveAllAndFlush(List.of(requestedPlayer, offeredPlayer));

        teamPlayerRepository.save(new TeamPlayer(
                trade.getProposingTeam(), trade.getProposingTeam().getLeague(),
                requestedPlayer.getPlayer(), transferDate, requestedPlayer.getPurchasePrice()));
        teamPlayerRepository.save(new TeamPlayer(
                trade.getReceivingTeam(), trade.getReceivingTeam().getLeague(),
                offeredPlayer.getPlayer(), transferDate, offeredPlayer.getPurchasePrice()));

        trade.setStatus(TradeStatus.ACCEPTED);
        tradeRepository.save(trade);
    }

}
