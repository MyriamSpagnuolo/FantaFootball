package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.PasswordResetToken;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.PasswordResetTokenRepository;
import org.generation.italy.fantafootball.security.AppPasswordResetProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PasswordResetService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);
    private final AppUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetMailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AppPasswordResetProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(AppUserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordResetMailService mailService,
                                PasswordEncoder passwordEncoder,
                                AppPasswordResetProperties properties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(AppUser::isEnabled)
                .ifPresent(user -> {
                    tokenRepository.deleteAllByUserId(user.getId());
                    String rawToken = generateToken();
                    Instant now = Instant.now();
                    tokenRepository.save(new PasswordResetToken(
                            user, hash(rawToken), now, now.plus(properties.ttl())));
                    try {
                        mailService.send(user.getEmail(), rawToken);
                    } catch (MailException exception) {
                        LOGGER.error("Impossibile inviare l'email di recupero password", exception);
                    }
                });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        Instant now = Instant.now();
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .filter(value -> value.getUsedAt() == null)
                .filter(value -> value.getExpiresAt().isAfter(now))
                .filter(value -> value.getUser().isEnabled())
                .orElseThrow(() -> new BadRequestException(
                        "invalid_reset_token", "Il link di recupero non è valido o è scaduto"));

        token.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
        token.getUser().incrementTokenVersion();
        token.setUsedAt(now);
        tokenRepository.deleteAllByUserId(token.getUser().getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 non disponibile", exception);
        }
    }
}
