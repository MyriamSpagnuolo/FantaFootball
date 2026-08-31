package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTradeRequest(
        @NotNull(message = "La squadra ricevente è obbligatoria")
        Long receivingTeamId,

        @NotNull(message = "Il giocatore richiesto è obbligatorio")
        Long requestedPlayerId,

        @NotNull(message = "Il giocatore offerto è obbligatorio")
        Long offeredPlayerId,

        Integer amount
) {
}
