package org.generation.italy.fantafootball.integration.leaguesim;

import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimMatchdayDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerResultDto;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.repositories.MatchdayRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Questa classe e' separata da LeagueSimSyncService per un motivo preciso, non a caso:
// @Transactional in Spring funziona tramite un "proxy" che intercetta le chiamate dall'ESTERNO
// della classe. Se il metodo transazionale fosse chiamato da un altro metodo della STESSA classe
// (es. this.importResults(...) dentro un ciclo in LeagueSimSyncService), la chiamata bypasserebbe
// il proxy e @Transactional verrebbe semplicemente ignorato — con il rischio concreto di lasciare
// nel database una giornata segnata come "chiusa" (closed = true) senza che tutti i suoi risultati
// siano stati salvati, se una richiesta a meta' lista fallisce. Mettendo il metodo in un bean
// diverso, la chiamata da LeagueSimSyncService passa correttamente attraverso il proxy.
@Service
public class LeagueSimMatchdayImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeagueSimMatchdayImportService.class);

    private final MatchdayRepository matchdayRepository;
    private final PlayerRepository playerRepository;
    private final PlayerResultRepository playerResultRepository;

    public LeagueSimMatchdayImportService(MatchdayRepository matchdayRepository,
                                           PlayerRepository playerRepository,
                                           PlayerResultRepository playerResultRepository) {
        this.matchdayRepository = matchdayRepository;
        this.playerRepository = playerRepository;
        this.playerResultRepository = playerResultRepository;
    }

    // @Transactional qui vuol dire: o va tutto a buon fine (tutti i PlayerResult salvati E la
    // giornata segnata closed = true), o in caso di eccezione viene annullato tutto (rollback) —
    // cosi' un fallimento a meta' non lascia lo stato incoerente, e il prossimo giro di sync
    // ritentera' l'intera giornata da capo (findByNumber/findByPlayerIdAndMatchdayId sotto fanno
    // "trova o crea", quindi ripetere l'operazione e' sicuro: non si creano duplicati).
    @Transactional
    public void importResults(LeagueSimMatchdayDto matchdayDto, List<LeagueSimPlayerResultDto> results) {
        Matchday matchday = matchdayRepository.findByNumber(matchdayDto.number())
                .orElseGet(() -> new Matchday(matchdayDto.number(), matchdayDto.date()));

        // Se la giornata e' nuova non ha ancora un id (viene generato dal DB al primo salvataggio):
        // dobbiamo salvarla subito per poterla referenziare come FK nei PlayerResult qui sotto.
        matchday = matchdayRepository.save(matchday);

        // Il sync dei player gira molto meno spesso di quello dei risultati (per scelta: i prezzi/
        // infortuni cambiano poco, i risultati vanno presi appena la giornata chiude). Questo pero'
        // apre un caso reale: un giocatore nuovo su LeagueSim potrebbe non essere ancora presente
        // in locale quando arriviamo qui. Se marcassimo comunque closed = true, quella giornata
        // resterebbe incompleta per sempre (isAlreadyImported la salterebbe per tutti i prossimi
        // giri). Per questo teniamo traccia se TUTTI i giocatori sono stati risolti, e marchiamo
        // la giornata chiusa solo in quel caso: altrimenti il prossimo sync risultati la ritentera'.
        boolean allPlayersResolved = true;
        for (LeagueSimPlayerResultDto resultDto : results) {
            if (!importSingleResult(matchday, resultDto)) {
                allPlayersResolved = false;
            }
        }

        if (allPlayersResolved) {
            matchday.setClosed(true);
            matchdayRepository.save(matchday);
        } else {
            LOGGER.warn("Giornata {} non segnata come importata: uno o piu' giocatori non ancora "
                    + "sincronizzati, verra' ritentata al prossimo sync dei risultati", matchday.getNumber());
        }
    }

    // Ritorna true se il risultato e' stato importato, false se e' stato saltato (giocatore non
    // ancora noto in locale) — il chiamante usa questo esito per decidere se la giornata e' completa.
    private boolean importSingleResult(Matchday matchday, LeagueSimPlayerResultDto resultDto) {
        Optional<Player> player = playerRepository.findByExternalId(resultDto.playerId());
        if (player.isEmpty()) {
            LOGGER.warn("Player esterno con id {} non trovato in locale: risultato saltato per la giornata {}",
                    resultDto.playerId(), matchday.getNumber());
            return false;
        }

        PlayerResult playerResult = playerResultRepository
                .findByPlayerIdAndMatchdayId(player.get().getId(), matchday.getId())
                .orElseGet(PlayerResult::new);

        playerResult.setPlayer(player.get());
        playerResult.setMatchday(matchday);
        playerResult.setRating(resultDto.rating());
        playerResult.setGoalNum(resultDto.goals());
        playerResult.setGoalConceded(resultDto.goalsConceded());
        playerResult.setAutogoalNum(resultDto.ownGoals());
        playerResult.setAssistNum(resultDto.assists());
        playerResult.setPenaltySaved(resultDto.penaltySaved());
        playerResult.setPenaltyFailed(resultDto.penaltyFailed());
        playerResult.setCleanSheet(resultDto.cleanSheet());
        playerResult.setYellowCard(resultDto.yellowCards());
        playerResult.setRedCard(resultDto.redCard());

        playerResultRepository.save(playerResult);
        return true;
    }
}
