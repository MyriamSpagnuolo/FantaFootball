package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;

public record DisableAccountRequest(
        @NotBlank(message = "La password corrente è obbligatoria")
        String currentPassword
) {
}
