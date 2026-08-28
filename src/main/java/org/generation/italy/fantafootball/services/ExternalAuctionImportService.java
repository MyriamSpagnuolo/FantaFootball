package org.generation.italy.fantafootball.services;

import jakarta.transaction.Transactional;
import org.generation.italy.fantafootball.model.dto.AuctionPlayerImportRequest;
import org.generation.italy.fantafootball.model.dto.AuctionRosterImportRequest;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class ExternalAuctionImportService {
    private final TeamPlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    public ExternalAuctionImportService(TeamPlayerRepository playerRepository,
                                        LeagueRepository leagueRepository,
                                        TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public void importRoster(Long leagueId, Long teamId, Long authenticatedUserId,
                             AuctionRosterImportRequest roster) {
        League league = findLeague(leagueId);
        Team team = findTeam(teamId);
        validateTeamBelongsToLeague(team, league);
        validateAdmin(league, authenticatedUserId);
        List<AuctionPlayerImportRequest> players = validateRoster(roster);
        long totalPrice = calculateBudget(players);
        validateBudget(totalPrice,team);
        List<TeamPlayer> teamPlayers = serializeImportedPlayers(players,team);
        playerRepository.saveAll(teamPlayers);
        team.setBudget((int) (team.getBudget() - totalPrice));
        teamRepository.save(team);
    }

    //controllo se il budget della squadra permette di trasferire i giocatori
    private void validateBudget(long totalPrice,Team team) {
        if(totalPrice > team.getBudget()) {
            throw new ConflictException(
                    "budget_too_low",
                    "Impossibile eseguire l'operazione budget insufficiente"
            );
        }
    }

    //trasformi i player ricevuti dal servizio esterno in teamPlayer
    private List<TeamPlayer> serializeImportedPlayers(List<AuctionPlayerImportRequest> players, Team team) {
        return players.stream()
                .map(player ->
                        new TeamPlayer(team, player.name(), player.surname(),
                        player.realTeamName(), player.realTeamShirtNum(), player.purchasePrice(),
                        LocalDate.now(), player.purchasePrice()))
                .toList();
    }

    //tramite i metodi crud verifico che esistano sia la lega che la squadra
    private League findLeague(Long id) {
        if (id == null) throw new BadRequestException("invalid_league_id", "L'ID della lega è obbligatorio");
        return leagueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("league_not_found", "Lega non esistente"));
    }

    private Team findTeam(Long id) {
        if (id == null) throw new BadRequestException(
                "invalid_team_id",
                "L'ID della squadra è obbligatorio");
        return teamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "team_not_found",
                        "Questo team non esiste"));
    }

    //controllo se l'admin che ha fatto la richiesta sia effettivamente l'admin della lega su cui vuole inserire i player
    private void validateAdmin(League league, Long authenticatedUserId) {
        if (!Objects.equals(league.getAdmin().getId(), authenticatedUserId))
            throw new AccessDeniedException(
                    "Solo l'admin della lega può importare giocatori");
    }

    //controllo che la lega del team su cui andrò ad inserire i giocatori sia effetivamente la lega presa in input
    private void validateTeamBelongsToLeague(Team team, League league) {
        if (!Objects.equals(team.getLeague().getId(), league.getId()))
            throw new BadRequestException(
                    "league_id_not_valid",
                    "Il team non appartiene alla lega indicata");
    }

    //controllo prima che non sia vuota e poi che lo stato dei suoi DTO dentro la collection sia valido
    private List<AuctionPlayerImportRequest> validateRoster(AuctionRosterImportRequest roster) {
        if (roster == null || roster.players() == null || roster.players().isEmpty())
            throw new BadRequestException(
                    "list_empty",
                    "Non puoi trasferire una lista vuota");
        for (AuctionPlayerImportRequest player : roster.players()) {
            if (player == null || player.name() == null || player.name().isBlank()
                    || player.surname() == null || player.surname().isBlank()
                    || player.realTeamName() == null || player.realTeamName().isBlank()
                    || player.realTeamShirtNum() <= 0 || player.purchasePrice() < 0)
                throw new BadRequestException(
                        "invalid_player",
                        "Dati giocatore non validi");
        }
        return roster.players();
    }

    //trasformo la lista di DTO in un numero per potere calcolare il costo totale dei giocatori
    private Long calculateBudget(List<AuctionPlayerImportRequest>players) {
         return players.stream().mapToLong(AuctionPlayerImportRequest::purchasePrice).sum();
    }
}
