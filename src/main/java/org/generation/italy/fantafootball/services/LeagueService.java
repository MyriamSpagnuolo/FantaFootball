package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.CreateLeagueRequest;
import org.generation.italy.fantafootball.model.dto.LeagueResponse;
import org.generation.italy.fantafootball.model.dto.PlayerResponse;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final AppUserRepository appUserRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public LeagueService(LeagueRepository leagueRepository, AppUserRepository appUserRepository,
                         TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.leagueRepository = leagueRepository;
        this.appUserRepository = appUserRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> getAvailablePlayers(Long leagueId, Long requestingUserId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("LEAGUE_NOT_FOUND", "Lega non trovata: " + leagueId));

        boolean isAdmin = league.getAdmin().getId().equals(requestingUserId);
        if (!isAdmin && !teamRepository.existsByUserIdAndLeagueId(requestingUserId, leagueId)) {
            throw new AccessDeniedException("Devi far parte della lega per vedere i giocatori disponibili");
        }

        return playerRepository.findAvailableByLeagueId(leagueId).stream()
                .map(PlayerResponse::fromEntity)
                .toList();
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public LeagueResponse getLeagueById(Long leagueId, Long requestingUserId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("LEAGUE_NOT_FOUND", "Lega non trovata: " + leagueId));

        boolean isMember = teamRepository.existsByUserIdAndLeagueId(requestingUserId, leagueId);
        if (!isMember) {
            throw new AccessDeniedException("Devi far parte della lega per vederne i dettagli");
        }

        return LeagueResponse.fromEntity(league);
    }

    @Transactional
    public LeagueResponse createLeague(CreateLeagueRequest request, Long adminUserId) {
        AppUser admin = appUserRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Utente non trovato: " + adminUserId));

        String inviteCode = generateUniqueInviteCode();

        League league = new League(request.name(), inviteCode, admin);
        league.setCreationDate(LocalDateTime.now());
        league.setBudget(request.budget());

        League savedLeague = leagueRepository.save(league);
        return LeagueResponse.fromEntity(savedLeague);
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (leagueRepository.existsByInviteCode(code));
        return code;
    }
}