package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Team;

public record TeamResponse(
        Long id,
        String name,
        Long userId,
        Long leagueId,
        String leagueName,
        int budget,
        int totalPoints
) {
    public static TeamResponse fromEntity(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getUser().getId(),
                team.getLeague().getId(),
                team.getLeague().getName(),
                team.getBudget(),
                team.getTotalPoints()
        );
    }
}