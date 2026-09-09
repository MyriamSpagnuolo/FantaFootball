package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.PasswordResetToken;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.PasswordResetTokenRepository;
import org.generation.italy.fantafootball.security.AppPasswordResetProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private final AppUserRepository userRepository = mock(AppUserRepository.class);
    private final PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
    private final PasswordResetMailService mailService = mock(PasswordResetMailService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final PasswordResetService service = new PasswordResetService(
            userRepository, tokenRepository, mailService, passwordEncoder,
            new AppPasswordResetProperties(Duration.ofMinutes(30), "http://localhost/reset", "noreply@test.local"));

    @Test
    void requestResetCreatesHashedTokenAndSendsRawToken() {
        AppUser user = mock(AppUser.class);
        when(user.isEnabled()).thenReturn(true);
        when(user.getId()).thenReturn(7L);
        when(user.getEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        service.requestReset(" USER@example.com ");

        verify(tokenRepository).deleteAllByUserId(7L);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(mailService).send(eq("user@example.com"), anyString());
    }

    @Test
    void requestResetDoesNotRevealUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        service.requestReset("missing@example.com");

        verifyNoInteractions(tokenRepository, mailService);
    }

    @Test
    void resetPasswordChangesHashAndInvalidatesTokens() {
        AppUser user = mock(AppUser.class);
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.getUsedAt()).thenReturn(null);
        when(token.getExpiresAt()).thenReturn(Instant.now().plusSeconds(300));
        when(token.getUser()).thenReturn(user);
        when(user.isEnabled()).thenReturn(true);
        when(user.getId()).thenReturn(7L);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NuovaPassword1!")).thenReturn("new-hash");

        service.resetPassword("raw-token", "NuovaPassword1!");

        verify(user).setPasswordHash("new-hash");
        verify(user).incrementTokenVersion();
        verify(token).setUsedAt(any(Instant.class));
        verify(tokenRepository).deleteAllByUserId(7L);
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.getUsedAt()).thenReturn(null);
        when(token.getExpiresAt()).thenReturn(Instant.now().minusSeconds(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class,
                () -> service.resetPassword("expired-token", "NuovaPassword1!"));
    }
}
