package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;

import java.time.LocalDate;

public record TeamPlayerResponse(
        Long id, Long teamId, Long playerId,
        String name, String surname, String realTeamName, String playerRole, int realTeamShirtNum,
        boolean injured, LocalDate purchaseDate, LocalDate transferDate, int purchasePrice,
        Double fantaAverage
) {
    public static TeamPlayerResponse fromEntity(TeamPlayer teamPlayer, Double fantaAverage) {
        Player player = teamPlayer.getPlayer();
        return new TeamPlayerResponse(
                teamPlayer.getId(), teamPlayer.getTeam().getId(), player.getId(),

                player.getName(), player.getSurname(), player.getRealTeamName(), player.getRole().name(), player.getRealTeamShirtNum(),
                player.isInjured(), teamPlayer.getPurchaseDate(), teamPlayer.getTransferDate(), teamPlayer.getPurchasePrice(),
                fantaAverage
        );
    }
}
