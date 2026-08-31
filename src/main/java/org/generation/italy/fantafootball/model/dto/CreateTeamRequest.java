package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
        @NotBlank(message = "Il nome della squadra è obbligatorio")
        @Size(max = 100, message = "Il nome non può superare 100 caratteri")
        String name,

        @NotNull(message = "L'utente proprietario è obbligatorio")
        Long userId,
        //da cambiare perchè nella creazione della lega non devo passare l'id dell'utente che la crea.

        @NotNull(message = "La lega è obbligatoria")
        Long leagueId
) {
}