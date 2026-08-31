package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotNull;

public record RespondToInviteRequest(
        @NotNull(message = "L'utente che risponde è obbligatorio")
        Long respondingUserId
) {
}