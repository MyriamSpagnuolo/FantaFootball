package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Team;

import java.util.Objects;

public record UserLeagueTeamResponse(
        LeagueResponse league,
        TeamResponse team,
        boolean admin
) {
    public static UserLeagueTeamResponse fromEntity(Team team) {
        return new UserLeagueTeamResponse(
                LeagueResponse.fromEntity(team.getLeague()),
                TeamResponse.fromEntity(team),
                Objects.equals(team.getLeague().getAdmin().getId(), team.getUser().getId())
        );
    }
}
