package org.generation.italy.fantafootball.services;

import jakarta.transaction.Transactional;
import org.generation.italy.fantafootball.model.dto.PurchasePlayerRequest;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class ExternalAuctionImportService {
    private final TeamPlayerRepository teamPlayerRepository;
    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    public ExternalAuctionImportService(TeamPlayerRepository teamPlayerRepository,
                                        PlayerRepository playerRepository,
                                        LeagueRepository leagueRepository,
                                        TeamRepository teamRepository) {
        this.teamPlayerRepository = teamPlayerRepository;
        this.playerRepository = playerRepository;
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public void importPlayer(Long leagueId, Long teamId, Long authenticatedUserId,
                             Long playerId, PurchasePlayerRequest request) {
        int purchasePrice = request.purchasePrice();

        League league = findLeague(leagueId);
        Team team = findTeam(teamId);
        Player player = findPlayer(playerId);

        validateTeamBelongsToLeague(team, league);
        validateAdmin(league, authenticatedUserId);
        validatePurchaseRequest(request);
        validatePlayerAvailable(player, league);
        validateBudget(purchasePrice, team);

        teamPlayerRepository.save(new TeamPlayer(
                team, league, player, LocalDate.now(), purchasePrice));
        team.setBudget(team.getBudget() - purchasePrice);
        teamRepository.save(team);
    }

    private Player findPlayer(Long id) {
        if (id == null) {
            throw new BadRequestException(
                    "invalid_player_id",
                    "L'ID del giocatore è obbligatorio");
        }
        return playerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "player_not_found",
                        "Giocatore non esistente"));
    }

    private void validatePlayerAvailable(Player player, League league) {
        if (teamPlayerRepository.existsByPlayer_IdAndLeague_IdAndTransferDateIsNull(
                player.getId(), league.getId())) {
            throw new ConflictException(
                    "player_already_owned",
                    "Il giocatore è già stato acquistato nella lega");
        }
    }

    private void validateBudget(int purchasePrice, Team team) {
        if (purchasePrice > team.getBudget()) {
            throw new ConflictException(
                    "budget_too_low",
                    "Impossibile eseguire l'operazione: budget insufficiente");
        }
    }

    private void validatePurchaseRequest(PurchasePlayerRequest request) {
        if (request == null || request.purchasePrice() == null || request.purchasePrice() < 0) {
            throw new BadRequestException(
                    "invalid_purchase_price",
                    "Il prezzo di acquisto deve essere maggiore o uguale a zero");
        }
    }

    private League findLeague(Long id) {
        if (id == null) {
            throw new BadRequestException(
                    "invalid_league_id",
                    "L'ID della lega è obbligatorio");
        }
        return leagueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "league_not_found",
                        "Lega non esistente"));
    }

    private Team findTeam(Long id) {
        if (id == null) {
            throw new BadRequestException(
                    "invalid_team_id",
                    "L'ID della squadra è obbligatorio");
        }
        return teamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "team_not_found",
                        "Questo team non esiste"));
    }

    private void validateAdmin(League league, Long authenticatedUserId) {
        if (!Objects.equals(league.getAdmin().getId(), authenticatedUserId)) {
            throw new AccessDeniedException(
                    "Solo l'admin della lega può importare giocatori");
        }
    }

    private void validateTeamBelongsToLeague(Team team, League league) {
        if (!Objects.equals(team.getLeague().getId(), league.getId())) {
            throw new BadRequestException(
                    "league_id_not_valid",
                    "Il team non appartiene alla lega indicata");
        }
    }

}
