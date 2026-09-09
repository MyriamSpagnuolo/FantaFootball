package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Lineup;

import java.util.List;

public record LineupResponse(
        Long id,
        Long teamId,
        Long leagueMatchId,
        Long lineupTypeId,
        boolean defensive,
        List<LineupPlayerResponse> players
) {
    public static LineupResponse fromEntity(Lineup lineup, List<LineupPlayerResponse> players) {
        return new LineupResponse(
                lineup.getId(),
                lineup.getTeam().getId(),
                lineup.getLeagueMatch().getId(),
                lineup.getLineupType().getId(),
                lineup.isDefensive(),
                players
        );
    }
}
