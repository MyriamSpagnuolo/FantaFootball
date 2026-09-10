package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.PlayerRole;
import java.util.List;

public record PlayerFilterRequest(
        List<PlayerRole> role,
        List<String> realTeamName,
        Integer minPrice,
        Integer maxPrice,
        Boolean injured,
        String search
) {
    public PlayerFilterRequest(PlayerRole role, String realTeamName, Integer minPrice, Integer maxPrice, Boolean injured) {
        this(role == null ? List.of() : List.of(role),
                realTeamName == null ? List.of() : List.of(realTeamName), minPrice, maxPrice, injured, null);
    }
}
