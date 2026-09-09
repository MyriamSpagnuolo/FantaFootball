package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameTeamRequest(
        @NotBlank(message = "Il nome della squadra è obbligatorio")
        @Size(max = 100, message = "Il nome non può superare 100 caratteri")
        String name
) {
}
