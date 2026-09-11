package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;

public record LineupPlayerResponse(
        Long teamPlayerId,
        Long playerId,
        String name,
        String surname,
        PlayerRole role,
        boolean starter
) {
    public static LineupPlayerResponse fromEntity(LineupPlayer lineupPlayer) {
        Player player = lineupPlayer.getTeamPlayer().getPlayer();
        return new LineupPlayerResponse(
                lineupPlayer.getTeamPlayer().getId(),
                player.getId(),
                player.getName(),
                player.getSurname(),
                player.getRole(),
                lineupPlayer.isStarter()
        );
    }
}
