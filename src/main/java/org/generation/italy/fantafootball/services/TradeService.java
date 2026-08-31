package org.generation.italy.fantafootball.services;

import jakarta.transaction.Transactional;
import org.generation.italy.fantafootball.model.dto.CreateTradeRequest;
import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.generation.italy.fantafootball.model.repositories.TradeRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
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
    private final TeamRepository teamRepository;

    public TradeService(TradeRepository tradeRepository, TeamPlayerRepository teamPlayerRepository,
                        TeamRepository teamRepository) {
        this.tradeRepository = tradeRepository;
        this.teamPlayerRepository = teamPlayerRepository;
        this.teamRepository = teamRepository;
    }

    public List<TradeDto> getAllByUserId(Long userId) {
        return toDtos(tradeRepository.findAllByUserId(userId));
    }

    public List<TradeDto> getAllPendingSentTradesByTeamId(Long teamId, Long userId) {
        findOwnedTeam(teamId, userId);
        return toDtos(tradeRepository.findByProposingTeam_IdAndStatus(teamId, TradeStatus.PENDING));
    }

    public List<TradeDto> getAllPendingReceivedTradesByTeamId(Long teamId, Long userId) {
        findOwnedTeam(teamId, userId);
        return toDtos(tradeRepository.findByReceivingTeam_IdAndStatus(teamId, TradeStatus.PENDING));
    }

    public List<TradeDto> getTradeHistoryByTeamId(Long teamId, Long userId) {
        findOwnedTeam(teamId, userId);
        return toDtos(tradeRepository.findTradeHistoryByTeamId(teamId));
    }

    @Transactional
    public TradeDto createTrade(CreateTradeRequest request, Long proposingUserId) {
        Team receivingTeam = teamRepository.findById(request.receivingTeamId())
                .orElseThrow(() -> notFound("team_not_found", "Receiving team not found"));
        Team proposingTeam = findTeamOwnedByUserInLeague(proposingUserId, receivingTeam);

        if (Objects.equals(proposingTeam.getId(), receivingTeam.getId())) {
            throw conflict("invalid_teams", "A team cannot trade with itself");
        }
        validateSameLeague(proposingTeam, receivingTeam);

        TeamPlayer requestedPlayer = findPlayer(request.requestedPlayerId());
        TeamPlayer offeredPlayer = findPlayer(request.offeredPlayerId());
        validatePlayerBelongsToTeam(requestedPlayer, receivingTeam, "requested_player_not_owned");
        validatePlayerBelongsToTeam(offeredPlayer, proposingTeam, "offered_player_not_owned");
        validatePlayerAvailable(requestedPlayer);
        validatePlayerAvailable(offeredPlayer);
        validateAmount(request.amount());

        Trade trade = new Trade(proposingTeam, receivingTeam, requestedPlayer, offeredPlayer, TradeStatus.PENDING);
        trade.setAmount(request.amount());
        trade.setProposalDate(java.time.LocalDateTime.now());
        return TradeDto.fromEntity(tradeRepository.save(trade));
    }

    @Transactional
    public void rejectTradeById(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findByIdForUpdate(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));
        validateParticipant(trade, userId);
        validatePending(trade, "rejected");

        trade.setStatus(TradeStatus.REJECTED);
        tradeRepository.save(trade);
    }

    @Transactional
    public void acceptTradeById(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findByIdForUpdate(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));
        validateReceiver(trade, userId);
        validatePending(trade, "accepted");
        validatePlayersStillAvailable(trade);
        lockTeams(trade);
        settleAmount(trade);
        transferPlayers(trade);

        trade.setStatus(TradeStatus.ACCEPTED);
        tradeRepository.save(trade);
    }

    private Team findTeamOwnedByUserInLeague(Long userId, Team receivingTeam) {
        return teamRepository.findFirstByUserIdAndLeagueId(userId, receivingTeam.getLeague().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not own a team in this league"));
    }

    private Team findOwnedTeam(Long teamId, Long userId) {
        if (userId == null) {
            return teamRepository.findById(teamId)
                    .orElseThrow(() -> notFound("team_not_found", "Team not found"));
        }
        return teamRepository.findByIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not own this team"));
    }

    private TeamPlayer findPlayer(Long playerId) {
        return teamPlayerRepository.findById(playerId)
                .orElseThrow(() -> notFound("player_not_found", "Player not found"));
    }

    private void validateSameLeague(Team first, Team second) {
        if (!Objects.equals(first.getLeague().getId(), second.getLeague().getId())) {
            throw conflict("different_leagues", "Both teams must belong to the same league");
        }
    }

    private void validatePlayerBelongsToTeam(TeamPlayer player, Team team, String errorCode) {
        if (!Objects.equals(player.getTeam().getId(), team.getId())
                || !Objects.equals(player.getLeague().getId(), team.getLeague().getId())) {
            throw conflict(errorCode, "The player does not belong to the expected team");
        }
    }

    private void validatePlayerAvailable(TeamPlayer player) {
        if (player.getTransferDate() != null) {
            throw conflict("player_not_available", "The player is no longer available");
        }
    }

    private void validateAmount(Integer amount) {
        if (amount != null && amount == Integer.MIN_VALUE) {
            throw conflict("invalid_amount", "The amount is too large");
        }
    }

    private void lockTeams(Trade trade) {
        Team first = trade.getProposingTeam().getId() < trade.getReceivingTeam().getId()
                ? trade.getProposingTeam() : trade.getReceivingTeam();
        Team second = first == trade.getProposingTeam()
                ? trade.getReceivingTeam() : trade.getProposingTeam();
        teamRepository.findByIdForUpdate(first.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "A trade team no longer exists"));
        teamRepository.findByIdForUpdate(second.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "A trade team no longer exists"));
    }

    private ResponseStatusException conflict(String code, String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, code + ": " + message);
    }

    private ResponseStatusException notFound(String code, String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, code + ": " + message);
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

        long proposingBudget = proposingTeam.getBudget() - amount;
        long receivingBudget = receivingTeam.getBudget() + amount;
        if (proposingBudget < 0 || receivingBudget < 0
                || proposingBudget > Integer.MAX_VALUE || receivingBudget > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The resulting budget is invalid");
        }
        proposingTeam.setBudget((int) proposingBudget);
        receivingTeam.setBudget((int) receivingBudget);
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
