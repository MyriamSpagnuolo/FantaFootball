package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotNull;

public record LineupPlayerRequest(
        @NotNull(message = "Il teamPlayerId è obbligatorio")
        Long teamPlayerId,

        boolean starter
) {
}
