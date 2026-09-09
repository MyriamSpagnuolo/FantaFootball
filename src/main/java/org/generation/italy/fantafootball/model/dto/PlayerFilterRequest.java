package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.PlayerRole;

public record PlayerFilterRequest(
        PlayerRole role,
        String realTeamName,
        Integer minPrice,
        Integer maxPrice,
        Boolean injured
) {
}
