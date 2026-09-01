package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotNull;
import org.generation.italy.fantafootball.model.entities.TradeStatus;

public record UpdateTradeStatusRequest(
        @NotNull(message = "Lo stato dello scambio e' obbligatorio")
        TradeStatus status
) {
}
