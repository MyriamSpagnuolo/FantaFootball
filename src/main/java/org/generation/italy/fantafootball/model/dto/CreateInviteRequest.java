package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInviteRequest(
        @NotBlank(message = "Lo username dell'utente da invitare è obbligatorio")
        String invitedUsername
) {
}
