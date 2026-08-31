package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.dto.UserLeagueTeamResponse;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {
    private final AppUserRepository appUserRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            AppUserRepository appUserRepository,
            TeamRepository teamRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserLeagueTeamResponse> getLeaguesAndTeams(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new NotFoundException("user_not_found", "Utente non trovato");
        }

        return teamRepository.findAllByUserIdOrderByLeagueNameAsc(userId).stream()
                .map(UserLeagueTeamResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void disableAccount(Long userId, String currentPassword) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found", "Utente non trovato"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("invalid_password", "La password corrente non è corretta");
        }

        user.setEnabled(false);
    }
}
