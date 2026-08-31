package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {
    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AccountService accountService = new AccountService(appUserRepository, teamRepository, passwordEncoder);

    @Test
    void getLeaguesAndTeamsReturnsAuthenticatedUsersMemberships() {
        AppUser user = new AppUser();
        League league = new League();
        league.setName("Serie A");
        league.setAdmin(user);
        Team team = new Team("I Bomber", user, league);
        when(appUserRepository.existsById(7L)).thenReturn(true);
        when(teamRepository.findAllByUserIdOrderByLeagueNameAsc(7L)).thenReturn(List.of(team));

        var result = accountService.getLeaguesAndTeams(7L);

        assertEquals(1, result.size());
        assertEquals("Serie A", result.getFirst().league().name());
        assertEquals("I Bomber", result.getFirst().team().name());
        assertTrue(result.getFirst().admin());
    }

    @Test
    void disableAccountDisablesUserWhenPasswordMatches() {
        AppUser user = new AppUser();
        user.setEnabled(true);
        user.setPasswordHash("encoded-password");
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded-password")).thenReturn(true);

        accountService.disableAccount(7L, "Password1!");

        assertFalse(user.isEnabled());
        verify(passwordEncoder).matches("Password1!", "encoded-password");
    }

    @Test
    void disableAccountRejectsWrongPassword() {
        AppUser user = new AppUser();
        user.setEnabled(true);
        user.setPasswordHash("encoded-password");
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> accountService.disableAccount(7L, "wrong"));

        assertTrue(user.isEnabled());
    }

    @Test
    void updateUsernameChangesUsernameAndRevokesExistingTokens() {
        AppUser user = new AppUser();
        user.setUsername("old.username");
        user.setPasswordHash("encoded-password");
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded-password")).thenReturn(true);
        when(appUserRepository.existsByUsernameIgnoreCaseAndIdNot("new.username", 7L)).thenReturn(false);

        accountService.updateUsername(7L, " new.username ", "Password1!");

        assertEquals("new.username", user.getUsername());
        assertEquals(1, user.getTokenVersion());
        verify(appUserRepository).saveAndFlush(user);
    }

    @Test
    void changePasswordEncodesPasswordAndRevokesExistingTokens() {
        AppUser user = new AppUser();
        user.setPasswordHash("old-hash");
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPassword1!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123!", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-hash");

        accountService.changePassword(7L, "CurrentPassword1!", "NewPassword123!");

        assertEquals("new-hash", user.getPasswordHash());
        assertEquals(1, user.getTokenVersion());
    }
}
