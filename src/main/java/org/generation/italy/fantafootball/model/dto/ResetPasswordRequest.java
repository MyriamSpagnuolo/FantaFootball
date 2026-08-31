package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.generation.italy.fantafootball.model.validation.StrongPassword;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @StrongPassword String newPassword
) {}
