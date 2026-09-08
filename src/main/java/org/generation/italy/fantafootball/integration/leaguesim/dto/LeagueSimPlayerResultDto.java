package org.generation.italy.fantafootball.integration.leaguesim.dto;

import java.math.BigDecimal;

// Il risultato di un giocatore in una giornata, cosi' come lo restituisce LeagueSim
// (GET /api/matchdays/{number}/results). Nota: LeagueSim manda solo "playerId", non nome/cognome/
// squadra del giocatore. Per capire di quale Player locale si tratta, chi usera' questo DTO dovra'
// incrociare playerId con l'externalId salvato sui nostri Player (dopo averli sincronizzati
// con fetchPlayers()) — non possiamo identificarlo da questo DTO da solo.
public record LeagueSimPlayerResultDto(
        Long playerId,
        BigDecimal rating,
        int goals,
        int goalsConceded,
        int ownGoals,
        int assists,
        int penaltySaved,
        int penaltyFailed,
        boolean cleanSheet,
        int yellowCards,
        boolean redCard
) {}
