package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.TeamPlayer;

import java.time.LocalDate;

public record TeamPlayerResponse(
        Long id,
        Long teamId,
        String name,
        String surname,
        String realTeamName,
        int realTeamShirtNum,
        int price,
        boolean injured,
        LocalDate purchaseDate,
        LocalDate transferDate,
        int purchasePrice
) {
    public static TeamPlayerResponse fromEntity(TeamPlayer player) {
        return new TeamPlayerResponse(
                player.getId(),
                player.getTeam().getId(),
                player.getName(),
                player.getSurname(),
                player.getRealTeamName(),
                player.getRealTeamShirtNum(),
                player.getPrice(),
                player.isInjured(),
                player.getPurchaseDate(),
                player.getTransferDate(),
                player.getPurchasePrice()
        );
    }
}