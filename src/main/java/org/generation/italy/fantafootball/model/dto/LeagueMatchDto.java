package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;

import java.time.LocalDateTime;

public record LeagueMatchDto(
        Long id,
        int roundNumber,
        LocalDateTime matchDay,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName
) {
    public static LeagueMatchDto from(LeagueMatch match) {
        return new LeagueMatchDto(
                match.getId(),
                match.getRoundNumber(),
                match.getMatchDay(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName()
        );
    }
}
