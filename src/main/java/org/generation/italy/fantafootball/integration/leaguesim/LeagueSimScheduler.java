package org.generation.italy.fantafootball.integration.leaguesim;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Punto di ingresso "automatico" della sincronizzazione: il solo scopo di questa classe e'
// decidere OGNI QUANTO richiamare LeagueSimSyncService, non COSA fare (quello resta nel service).
// Tenerla separata rende LeagueSimSyncService testabile senza bisogno di uno Spring context con
// scheduling attivo, e rende visibile a colpo d'occhio quali sono i "trigger" automatici del sistema.
//
// Due scheduler distinti, con frequenze diverse, per un motivo concreto:
// - le giornate (calendario + risultati quando disponibili) vanno controllate spesso: sia perche'
//   una giornata puo' chiudersi da un momento all'altro su LeagueSim, sia perche' il calendario
//   delle giornate future serve a generare i league_match il prima possibile;
// - il catalogo giocatori (player) cambia raramente (prezzo, infortuni), quindi controllarlo cosi'
//   spesso sarebbe inutile: un intervallo lungo basta e avanza.
// fixedDelay (invece di fixedRate) fa partire il conteggio del prossimo giro SOLO dopo che quello
// precedente e' finito: se una sincronizzazione impiega piu' del previsto, non si sovrappone alla
// successiva.
@Component
public class LeagueSimScheduler {

    private final LeagueSimSyncService syncService;

    public LeagueSimScheduler(LeagueSimSyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedDelayString = "${leaguesim.players-sync-interval}")
    public void syncPlayers() {
        syncService.syncPlayers();
    }

    @Scheduled(fixedDelayString = "${leaguesim.results-sync-interval}")
    public void syncMatchdays() {
        syncService.syncMatchdays();
    }
}
