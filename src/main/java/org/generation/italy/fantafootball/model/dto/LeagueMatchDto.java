package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeagueMatchDto(
        Long id,
        int roundNumber,
        LocalDateTime matchDay,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        BigDecimal homeScore,
        BigDecimal awayScore,
        Integer homeGoals,
        Integer awayGoals,
        boolean matchdayClosed
) {
    public static LeagueMatchDto from(LeagueMatch match) {
        return new LeagueMatchDto(
                match.getId(),
                match.getRoundNumber(),
                match.getMatchDay(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getHomeGoals(),
                match.getAwayGoals(),
                match.getMatchday().isClosed()
        );
    }
}
