package org.generation.italy.fantafootball.integration.leaguesim.dto;

import java.time.LocalDate;

// Una giornata di campionato cosi' come la restituisce LeagueSim (GET /api/matchdays).
// "closed" e' il segnale che ci interessa di piu': solo le giornate chiuse hanno risultati
// definitivi pronti da importare (vedi LeagueSimClient.fetchResults).
public record LeagueSimMatchdayDto(
        int number,
        LocalDate date,
        boolean closed
) {}
