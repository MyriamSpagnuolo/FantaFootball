package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotNull;

public record CreateInviteRequest(
        @NotNull(message = "L'utente che invita è obbligatorio")
        Long invitedByUserId,

        @NotNull(message = "L'utente invitato è obbligatorio")
        Long invitedUserId
) {
}
