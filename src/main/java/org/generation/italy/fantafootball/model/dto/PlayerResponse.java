package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;

public record PlayerResponse(
        Long id,
        Long externalId,
        String name,
        String surname,
        PlayerRole role,
        String realTeamName,
        int realTeamShirtNum,
        int price,
        boolean injured
) {

    public static PlayerResponse fromEntity(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getExternalId(),
                player.getName(),
                player.getSurname(),
                player.getRole(),
                player.getRealTeamName(),
                player.getRealTeamShirtNum(),
                player.getPrice(),
                player.isInjured()
        );
    }
}
