package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotNull;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;

public record UpdateInviteStatusRequest(
        @NotNull(message = "Lo stato dell'invito e' obbligatorio")
        LeagueInviteStatus status
) {
}
