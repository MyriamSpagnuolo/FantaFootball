package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.CreateTeamRequest;
import org.generation.italy.fantafootball.model.dto.TeamPlayerResponse;
import org.generation.italy.fantafootball.model.dto.TeamResponse;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final AppUserRepository appUserRepository;
    private final LeagueRepository leagueRepository;

    public TeamService(TeamRepository teamRepository,
                       TeamPlayerRepository teamPlayerRepository,
                       AppUserRepository appUserRepository,
                       LeagueRepository leagueRepository) {
        this.teamRepository = teamRepository;
        this.teamPlayerRepository = teamPlayerRepository;
        this.appUserRepository = appUserRepository;
        this.leagueRepository = leagueRepository;
    }

    public TeamResponse createTeam(CreateTeamRequest request) {
        boolean duplicate = teamRepository.existsByNameAndLeagueId(
                request.name(), request.leagueId());
        if (duplicate) {
            throw new ConflictException("DUPLICATE_TEAM_NAME",
                    "Esiste già una squadra con questo nome in questa lega");
        }

        Optional<AppUser> existingUser = appUserRepository.findById(request.userId());
        if (existingUser.isEmpty()) {
            throw new NotFoundException("USER_NOT_FOUND",
                    "Utente non trovato: " + request.userId());
        }
        AppUser user = existingUser.get();

        Optional<League> existingLeague = leagueRepository.findById(request.leagueId());
        if (existingLeague.isEmpty()) {
            throw new NotFoundException("LEAGUE_NOT_FOUND",
                    "Lega non trovata: " + request.leagueId());
        }
        League league = existingLeague.get();

        Team team = new Team(request.name(), user, league);
        Team saved = teamRepository.save(team);

        return TeamResponse.fromEntity(saved);
    }

    public List<TeamPlayerResponse> getTeamRoster(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new NotFoundException("TEAM_NOT_FOUND", "Squadra non trovata: " + teamId);
        }
        return teamPlayerRepository.findAllByTeamId(teamId).stream()
                .map(TeamPlayerResponse::fromEntity)
                .toList();
    }

    public void removePlayerFromTeam(Long teamId, Long playerId) {
        Optional<TeamPlayer> existingPlayer = teamPlayerRepository.findById(playerId);
        if (existingPlayer.isEmpty()) {
            throw new NotFoundException("PLAYER_NOT_FOUND", "Giocatore non trovato: " + playerId);
        }
        TeamPlayer player = existingPlayer.get();

        if (!player.getTeam().getId().equals(teamId)) {
            throw new ConflictException("PLAYER_NOT_IN_TEAM",
                    "Il giocatore non appartiene a questa squadra");
        }

        try {
            teamPlayerRepository.delete(player);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("PLAYER_IN_USE",
                    "Impossibile svincolare il giocatore: è presente in almeno una formazione (lineup)");
        }
    }
}