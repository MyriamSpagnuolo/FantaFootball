package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.CreateTeamRequest;
import org.generation.italy.fantafootball.model.dto.RenameTeamRequest;
import org.generation.italy.fantafootball.model.dto.TeamPlayerResponse;
import org.generation.italy.fantafootball.model.dto.TeamResponse;
import org.generation.italy.fantafootball.model.dto.TeamStandingResponse;
import org.generation.italy.fantafootball.model.entities.*;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueInviteRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final AppUserRepository appUserRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueInviteRepository leagueInviteRepository;

    public TeamService(TeamRepository teamRepository,
                       TeamPlayerRepository teamPlayerRepository,
                       AppUserRepository appUserRepository,
                       LeagueRepository leagueRepository,
                       LeagueInviteRepository leagueInviteRepository) {
        this.teamRepository = teamRepository;
        this.teamPlayerRepository = teamPlayerRepository;
        this.appUserRepository = appUserRepository;
        this.leagueRepository = leagueRepository;
        this.leagueInviteRepository = leagueInviteRepository;
    }

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, Long userId) {
        Optional<AppUser> existingUser = appUserRepository.findById(userId);
        if (existingUser.isEmpty()) {
            throw new NotFoundException("USER_NOT_FOUND", "Utente non trovato: " + userId);
        }
        AppUser user = existingUser.get();

        boolean duplicateName = teamRepository.existsByNameAndLeagueId(request.teamName(), request.leagueId());
        if (duplicateName) {
            throw new ConflictException("DUPLICATE_TEAM_NAME",
                    "Esiste già una squadra con questo nome in questa lega");
        }

        boolean userAlreadyHasTeam = teamRepository.existsByUserIdAndLeagueId(userId, request.leagueId());
        if (userAlreadyHasTeam) {
            throw new ConflictException("USER_ALREADY_HAS_TEAM",
                    "Questo utente ha già una squadra in questa lega");
        }

        Optional<League> existingLeague = leagueRepository.findById(request.leagueId());
        if (existingLeague.isEmpty()) {
            throw new NotFoundException("LEAGUE_NOT_FOUND", "Lega non trovata: " + request.leagueId());
        }
        League league = existingLeague.get();

        boolean isAdmin = userId.equals(league.getAdmin().getId());
        if(!isAdmin) {
            LeagueInvite invite = leagueInviteRepository
                    .findTopByLeagueIdAndInvitedUserIdOrderBySentDateDesc(request.leagueId(), userId)
                    .orElseThrow(() -> new AccessDeniedException(
                            "Non sei autorizzato a creare una squadra in questa lega"
                    ));

            if (invite.getStatus() == LeagueInviteStatus.PENDING) {
                throw new ConflictException(
                        "INVITE_PENDING",
                        "Devi accettare l'invito prima di creare una squadra"
                );
            }

            if (invite.getStatus() != LeagueInviteStatus.ACCEPTED) {
                throw new AccessDeniedException(
                        "Non sei autorizzato a creare una squadra in questa lega"
                );
            }
        }

        Team team = new Team(request.teamName(), user, league);
        team.setBudget(league.getBudget());
        Team saved = teamRepository.save(team);

        return TeamResponse.fromEntity(saved);
    }

    public TeamResponse renameTeam(Long teamId, Long requestingUserId, RenameTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("TEAM_NOT_FOUND", "Squadra non trovata: " + teamId));

        if (!team.getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Solo il proprietario può rinominare la squadra");
        }

        String newName = request.name().trim();
        boolean nameUnchanged = newName.equals(team.getName());
        if (!nameUnchanged && teamRepository.existsByNameAndLeagueId(newName, team.getLeague().getId())) {
            throw new ConflictException("DUPLICATE_TEAM_NAME",
                    "Esiste già una squadra con questo nome in questa lega");
        }

        team.setName(newName);
        Team saved = teamRepository.save(team);

        return TeamResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(Long requestingUserId) {
        return teamRepository.findAllByUserId(requestingUserId).stream()
                .map(TeamResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamStandingResponse> getTeamsByLeague(Long leagueId, Long requestingUserId) {
        if (!leagueRepository.existsById(leagueId)) {
            throw new NotFoundException("LEAGUE_NOT_FOUND", "Lega non trovata: " + leagueId);
        }
        boolean isMember = teamRepository.existsByUserIdAndLeagueId(requestingUserId, leagueId);
        if (!isMember) {
            throw new AccessDeniedException("Devi far parte della lega per vedere le squadre partecipanti");
        }

        return teamRepository.findAllTeamByLeagueId(leagueId).stream()
                .map(TeamStandingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
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
