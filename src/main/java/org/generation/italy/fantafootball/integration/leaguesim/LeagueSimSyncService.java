package org.generation.italy.fantafootball.integration.leaguesim;

import org.generation.italy.fantafootball.calculateMatchday.LeagueMatchScoreService;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimMatchdayDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerResultDto;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.repositories.MatchdayRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Orchestratore della sincronizzazione con LeagueSim: decide COSA sincronizzare e in che ordine,
// ma non contiene la logica HTTP (quella e' in LeagueSimClient) ne' quella di scrittura atomica
// dei risultati (quella e' in LeagueSimMatchdayImportService, per il motivo spiegato li').
// Il COME/QUANDO farla partire e' deciso da LeagueSimScheduler (@Scheduled): FantaFootball non
// espone e non esporra' un trigger manuale/admin per avviare la simulazione, resta un consumatore
// passivo che si limita a leggere periodicamente lo stato di LeagueSim.
@Service
public class LeagueSimSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeagueSimSyncService.class);

    private final LeagueSimClient client;
    private final PlayerRepository playerRepository;
    private final MatchdayRepository matchdayRepository;
    private final LeagueSimMatchdayImportService matchdayImportService;
    private final LeagueMatchScoreService leagueMatchScoreService;

    public LeagueSimSyncService(LeagueSimClient client,
                                 PlayerRepository playerRepository,
                                 MatchdayRepository matchdayRepository,
                                 LeagueSimMatchdayImportService matchdayImportService,
                                 LeagueMatchScoreService leagueMatchScoreService) {
        this.client = client;
        this.playerRepository = playerRepository;
        this.matchdayRepository = matchdayRepository;
        this.matchdayImportService = matchdayImportService;
        this.leagueMatchScoreService = leagueMatchScoreService;
    }

    // Allinea il catalogo giocatori locale con quello di LeagueSim: crea i Player mancanti e
    // aggiorna quelli gia' noti (prezzo e infortunio in particolare possono cambiare nel tempo).
    // Il confronto avviene per externalId (l'id di LeagueSim), MAI per nome/cognome: i nomi possono
    // avere errori di battitura o omonimie, l'id no.
    // Ogni giocatore e' isolato in un try/catch: un dato malformato su un giocatore non deve
    // impedire l'importazione di tutti gli altri.
    public void syncPlayers() {
        List<LeagueSimPlayerDto> remotePlayers;
        try {
            remotePlayers = client.fetchPlayers();
        } catch (Exception e) {
            // Se LeagueSim e' irraggiungibile non vogliamo che l'eccezione risalga fino allo
            // scheduler: verrebbe solo loggata genericamente da Spring. Logghiamo qui con un
            // messaggio chiaro e usciamo: il prossimo giro (schedulato piu' avanti) ritentera'.
            LOGGER.error("Impossibile recuperare il catalogo giocatori da LeagueSim: {}", e.getMessage(), e);
            return;
        }

        for (LeagueSimPlayerDto dto : remotePlayers) {
            try {
                upsertPlayer(dto);
            } catch (Exception e) {
                LOGGER.error("Impossibile sincronizzare il giocatore esterno con id {}: {}",
                        dto.id(), e.getMessage(), e);
            }
        }
    }

    private void upsertPlayer(LeagueSimPlayerDto dto) {
        Player player = playerRepository.findByExternalId(dto.id()).orElseGet(Player::new);
        player.setExternalId(dto.id());
        player.setName(dto.firstName());
        player.setSurname(dto.lastName());
        player.setRealTeamName(dto.realTeamName());
        player.setRealTeamShirtNum(dto.shirtNumber());
        player.setPrice(dto.price());
        player.setInjured(dto.injured());
        // PlayerRole.valueOf lancia IllegalArgumentException se la stringa non e' P/D/C/A:
        // la lasciamo propagare, viene gestita dal try/catch del chiamante (upsertPlayer si ferma
        // qui, il giocatore semplicemente non viene salvato in questo giro).
        player.setRole(PlayerRole.valueOf(dto.position()));
        playerRepository.save(player);
    }

    // Allinea il calendario locale con TUTTE le giornate note a LeagueSim, aperte o chiuse: anche
    // quelle non ancora giocate vengono salvate (solo number/date), perche' LeagueMatchService le
    // usa per generare il calendario (i league_match fittizi) di una lega prima ancora che quella
    // giornata sia stata disputata. Per le giornate che LeagueSim segna come chiuse (closed = true)
    // e non ancora importate in locale, importa anche i risultati. Usiamo il flag "closed" del
    // NOSTRO Matchday come marcatore "risultati gia' importati": se e' gia' true, saltiamo il
    // re-import (idempotenza — rilanciare questo metodo piu' volte non duplica ne' rifa' lavoro
    // inutile sui PlayerResult).
    // Ogni giornata e' isolata in un try/catch sull'upsert dell'anagrafica, come gia' fatto per i
    // giocatori in syncPlayers: senza questo isolamento, un errore sul salvataggio di UNA giornata
    // interrompeva il for e tutte le giornate successive della lista non venivano nemmeno salvate
    // (bug osservato: il calendario locale restava fermo a una sola giornata anche con piu' giornate
    // aperte su LeagueSim).
    // Per OGNI giornata gia' chiusa in locale (appena importata in questo giro, o chiusa in un giro
    // precedente) tenta anche il calcolo dei punteggi dei league_match tramite LeagueMatchScoreService
    // — a differenza dell'import dei risultati, QUESTO passo non va saltato con isAlreadyImported:
    // LeagueMatchScoreService.calculateAndSaveResultsForMatchday e' idempotente di suo (agisce solo
    // sui league_match ancora senza risultato), quindi richiamarla ad ogni giro e' economico e serve
    // a coprire il caso reale in cui un league_match/lineup viene creato DOPO che la giornata era
    // gia' stata chiusa (es. calendario generato o formazione schierata in un secondo momento, o
    // dopo un riavvio dell'app): senza questo, quel punteggio non verrebbe MAI calcolato, perche' il
    // "chiudersi" della giornata e' un evento che capita una volta sola.
    public void syncMatchdays() {
        List<LeagueSimMatchdayDto> remoteMatchdays;
        try {
            remoteMatchdays = client.fetchMatchdays();
        } catch (Exception e) {
            LOGGER.error("Impossibile recuperare l'elenco giornate da LeagueSim: {}", e.getMessage(), e);
            return;
        }

        for (LeagueSimMatchdayDto matchdayDto : remoteMatchdays) {
            try {
                upsertMatchdayShell(matchdayDto);
            } catch (Exception e) {
                LOGGER.error("Impossibile sincronizzare l'anagrafica della giornata {}: {}",
                        matchdayDto.number(), e.getMessage(), e);
                continue; // giornata non salvata: non ha senso tentare di importarne i risultati
            }

            if (!matchdayDto.closed()) {
                continue; // giornata non ancora giocata: non ci sono risultati da prendere
            }

            if (!isAlreadyImported(matchdayDto.number())) {
                try {
                    List<LeagueSimPlayerResultDto> results = client.fetchResults(matchdayDto.number());
                    matchdayImportService.importResults(matchdayDto, results);
                } catch (Exception e) {
                    // Un errore su una giornata (es. LeagueSim momentaneamente irraggiungibile) non
                    // deve interrompere il tentativo di importare le altre giornate del ciclo.
                    LOGGER.error("Impossibile importare i risultati della giornata {}: {}",
                            matchdayDto.number(), e.getMessage(), e);
                    continue; // senza risultati importati non ha senso tentare il calcolo dei punteggi
                }
            }

            calculateLeagueMatchScoresIfClosed(matchdayDto.number());
        }
    }

    private void calculateLeagueMatchScoresIfClosed(int matchdayNumber) {
        matchdayRepository.findByNumber(matchdayNumber)
                .filter(Matchday::isClosed)
                .ifPresent(matchday -> {
                    try {
                        leagueMatchScoreService.calculateAndSaveResultsForMatchday(matchday.getId());
                    } catch (Exception e) {
                        LOGGER.error("Impossibile calcolare i punteggi dei league_match per la giornata {}: {}",
                                matchdayNumber, e.getMessage(), e);
                    }
                });
    }

    // Crea o aggiorna solo l'anagrafica della giornata (number/date). Non tocca mai "closed" qui:
    // quel flag resta di competenza esclusiva di LeagueSimMatchdayImportService.importResults, che
    // lo marca true solo a import risultati completato (vedi il commento li').
    private void upsertMatchdayShell(LeagueSimMatchdayDto matchdayDto) {
        Matchday matchday = matchdayRepository.findByNumber(matchdayDto.number())
                .orElseGet(() -> new Matchday(matchdayDto.number(), matchdayDto.date()));
        matchday.setDate(matchdayDto.date());
        matchdayRepository.save(matchday);
    }

    private boolean isAlreadyImported(int matchdayNumber) {
        Optional<Matchday> local = matchdayRepository.findByNumber(matchdayNumber);
        return local.isPresent() && local.get().isClosed();
    }
}
