package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LineupRequest(
        @NotNull(message = "Il modulo (lineupTypeId) è obbligatorio")
        Long lineupTypeId,

        boolean defensive,

        @NotEmpty(message = "La formazione deve contenere almeno un giocatore")
        @Valid
        List<LineupPlayerRequest> players
) {
}
