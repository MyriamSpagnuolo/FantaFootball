package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Team;

public record TeamStandingResponse(
        Long teamId,
        String teamName,
        String username,
        int budget,
        int totalPoints
) {
    public static TeamStandingResponse fromEntity(Team team) {
        return new TeamStandingResponse(
                team.getId(),
                team.getName(),
                team.getUser().getUsername(),
                team.getBudget(),
                team.getTotalPoints()
        );
    }
}
