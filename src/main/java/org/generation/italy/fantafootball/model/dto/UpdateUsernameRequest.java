package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequest(
        @NotBlank(message = "Il nuovo username è obbligatorio")
        @Size(max = 80, message = "Lo username non può superare 80 caratteri")
        String newUsername,

        @NotBlank(message = "La password corrente è obbligatoria")
        String currentPassword
) {}
