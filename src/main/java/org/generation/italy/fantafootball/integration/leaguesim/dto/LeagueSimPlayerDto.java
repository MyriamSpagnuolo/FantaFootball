package org.generation.italy.fantafootball.integration.leaguesim.dto;

// Rappresenta un giocatore cosi' come lo restituisce LeagueSim (GET /api/players).
// I nomi dei campi ricalcano esattamente il JSON che LeagueSim risponde: Jackson (la libreria
// che converte JSON <-> oggetti Java) sa gia' mappare un record ai campi di un JSON con lo stesso
// nome, quindi non serve nessuna annotazione in piu' finche' i nomi combaciano.
//
// Volutamente e' un record separato da Player/PlayerResponse di fantafootball: e' solo la "fotografia"
// di cosa risponde LeagueSim in questo momento, non il nostro modello di dominio. Chi consumera'
// questo DTO (il futuro service di sincronizzazione) decidera' come convertirlo in un Player locale.
public record LeagueSimPlayerDto(
        Long id,
        String firstName,
        String lastName,
        String realTeamName,
        int shirtNumber,
        String position,
        int price,
        boolean injured
) {}
