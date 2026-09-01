package org.generation.italy.fantafootball.integration.leaguesim;

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
// Per ora questa classe va richiamata manualmente (es. da un test o da un endpoint temporaneo);
// il prossimo passo sara' decidere COME/QUANDO farla partire in automatico (scheduler, endpoint admin, ecc.).
@Service
public class LeagueSimSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeagueSimSyncService.class);

    private final LeagueSimClient client;
    private final PlayerRepository playerRepository;
    private final MatchdayRepository matchdayRepository;
    private final LeagueSimMatchdayImportService matchdayImportService;

    public LeagueSimSyncService(LeagueSimClient client,
                                 PlayerRepository playerRepository,
                                 MatchdayRepository matchdayRepository,
                                 LeagueSimMatchdayImportService matchdayImportService) {
        this.client = client;
        this.playerRepository = playerRepository;
        this.matchdayRepository = matchdayRepository;
        this.matchdayImportService = matchdayImportService;
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

    // Importa i risultati di tutte le giornate che LeagueSim segna come chiuse (closed = true) e
    // che non abbiamo ancora importato in locale. Usiamo il flag "closed" del NOSTRO Matchday
    // come marcatore "gia' importata": se e' gia' true, la saltiamo (idempotenza — rilanciare
    // questo metodo piu' volte non duplica ne' rifa' lavoro inutile).
    public void syncResults() {
        List<LeagueSimMatchdayDto> remoteMatchdays;
        try {
            remoteMatchdays = client.fetchMatchdays();
        } catch (Exception e) {
            LOGGER.error("Impossibile recuperare l'elenco giornate da LeagueSim: {}", e.getMessage(), e);
            return;
        }

        for (LeagueSimMatchdayDto matchdayDto : remoteMatchdays) {
            if (!matchdayDto.closed()) {
                continue; // giornata non ancora giocata: non ci sono risultati da prendere
            }
            if (isAlreadyImported(matchdayDto.number())) {
                continue;
            }

            try {
                List<LeagueSimPlayerResultDto> results = client.fetchResults(matchdayDto.number());
                matchdayImportService.importResults(matchdayDto, results);
            } catch (Exception e) {
                // Un errore su una giornata (es. LeagueSim momentaneamente irraggiungibile) non deve
                // interrompere il tentativo di importare le altre giornate del ciclo.
                LOGGER.error("Impossibile importare i risultati della giornata {}: {}",
                        matchdayDto.number(), e.getMessage(), e);
            }
        }
    }

    private boolean isAlreadyImported(int matchdayNumber) {
        Optional<Matchday> local = matchdayRepository.findByNumber(matchdayNumber);
        return local.isPresent() && local.get().isClosed();
    }
}
