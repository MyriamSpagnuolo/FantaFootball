package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLeagueRequest(
        @NotBlank(message = "Il nome della lega è obbligatorio")
        @Size(max = 100, message = "Il nome non può superare 100 caratteri")
        String name,

        @NotNull(message = "Il budget iniziale è obbligatorio")
        @Min(value = 0, message = "Il budget non può essere negativo")
        Integer budget
) {
}