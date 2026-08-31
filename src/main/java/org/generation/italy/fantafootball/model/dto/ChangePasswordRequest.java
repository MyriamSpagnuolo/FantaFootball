package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.generation.italy.fantafootball.model.validation.StrongPassword;

public record ChangePasswordRequest(
        @NotBlank(message = "La password corrente è obbligatoria")
        String currentPassword,

        @NotBlank(message = "La nuova password è obbligatoria")
        @StrongPassword
        String newPassword
) {}
