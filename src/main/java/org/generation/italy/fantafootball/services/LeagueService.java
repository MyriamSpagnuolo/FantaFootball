package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.CreateLeagueRequest;
import org.generation.italy.fantafootball.model.dto.LeagueResponse;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final AppUserRepository appUserRepository;

    public LeagueService(LeagueRepository leagueRepository, AppUserRepository appUserRepository) {
        this.leagueRepository = leagueRepository;
        this.appUserRepository = appUserRepository;
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