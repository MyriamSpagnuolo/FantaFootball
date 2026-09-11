package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;

// fantaRating e' null quando per questo giocatore non c'e' ancora un PlayerResult per la
// giornata richiesta (giornata non ancora chiusa, o il giocatore reale non ha preso parte alla
// partita simulata da LeagueSim) — non e' un errore, il chiamante lo interpreta come "nessun voto".
public record TeamPlayerRatingResponse(
        Long teamPlayerId,
        Long playerId,
        String name,
        String surname,
        PlayerRole role,
        Double fantaRating
) {
    public static TeamPlayerRatingResponse of(TeamPlayer teamPlayer, Double fantaRating) {
        var player = teamPlayer.getPlayer();
        return new TeamPlayerRatingResponse(
                teamPlayer.getId(),
                player.getId(),
                player.getName(),
                player.getSurname(),
                player.getRole(),
                fantaRating
        );
    }
}
