package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PurchasePlayerRequest(
        @NotNull @PositiveOrZero Integer purchasePrice
) {
}
