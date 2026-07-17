package org.generation.italy.fantafootball.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password",
            "password123",
            "qwerty",
            "admin",
            "letmein"
    );

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }

        String normalized = password.toLowerCase(Locale.ROOT);
        return password.length() >= 12
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))
                && !COMMON_PASSWORDS.contains(normalized);
    }
}
