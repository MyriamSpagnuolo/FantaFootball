package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.League;

import java.time.LocalDateTime;

public record LeagueResponse(
        Long id,
        String name,
        String inviteCode,
        Long adminUserId,
        LocalDateTime creationDate,
        int budget
) {
    public static LeagueResponse fromEntity(League league) {
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getInviteCode(),
                league.getAdmin().getId(),
                league.getCreationDate(),
                league.getBudget()
        );
    }
}
