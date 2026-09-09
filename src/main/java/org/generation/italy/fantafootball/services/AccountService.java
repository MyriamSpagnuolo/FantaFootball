package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.dto.UserLeagueTeamResponse;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
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
        AppUser user = getUserAndVerifyPassword(userId, currentPassword);
        user.setEnabled(false);
    }

    @Transactional
    public void updateUsername(Long userId, String newUsername, String currentPassword) {
        AppUser user = getUserAndVerifyPassword(userId, currentPassword);
        String normalizedUsername = newUsername.trim();

        if (appUserRepository.existsByUsernameIgnoreCaseAndIdNot(normalizedUsername, userId)) {
            throw new ConflictException("username_unavailable", "Lo username è già utilizzato");
        }
        if (user.getUsername().equals(normalizedUsername)) {
            throw new BadRequestException("username_unchanged", "Il nuovo username coincide con quello attuale");
        }

        user.setUsername(normalizedUsername);
        user.incrementTokenVersion();
        try {
            appUserRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("username_unavailable", "Lo username è già utilizzato");
        }
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        AppUser user = getUserAndVerifyPassword(userId, currentPassword);
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BadRequestException(
                    "password_unchanged", "La nuova password deve essere diversa da quella attuale");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.incrementTokenVersion();
    }

    private AppUser getUserAndVerifyPassword(Long userId, String currentPassword) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found", "Utente non trovato"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("invalid_password", "La password corrente non è corretta");
        }
        return user;
    }
}
