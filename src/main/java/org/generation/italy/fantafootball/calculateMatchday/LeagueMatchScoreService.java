package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.repositories.LeagueMatchRepository;
import org.generation.italy.fantafootball.model.repositories.LineupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Chiamato da LeagueSimSyncService.syncMatchdays ad OGNI giro di sync, per ogni giornata reale
// gia' chiusa (non solo al momento in cui si chiude): calcola e salva il risultato (home/away
// score e gol) di ogni league_match (la partita fittizia di lega) di quella giornata ancora senza
// risultato, in QUALSIASI lega, e aggiorna la classifica (Team.totalPoints) tramite Standing.
// Idempotente per costruzione: findAllByMatchdayIdAndHomeGoalsIsNull ignora i league_match gia'
// calcolati, quindi richiamarlo ripetutamente (ad ogni sync) non rifa' lavoro inutile ne' duplica
// punti in classifica. Questo e' voluto: un league_match (o la sua lineup) puo' essere creato DOPO
// che la giornata era gia' stata chiusa (calendario generato o formazione schierata in un secondo
// momento, o dopo un riavvio dell'app) — senza un ricalcolo ripetuto ad ogni giro, quel punteggio
// non verrebbe mai calcolato, perche' "chiudersi" e' un evento che a una Matchday capita una sola
// volta.
@Service
public class LeagueMatchScoreService {

    private final LeagueMatchRepository leagueMatchRepository;
    private final LineupRepository lineupRepository;
    private final MatchdayCalculationService matchdayCalculationService;
    private final Standing standing;

    public LeagueMatchScoreService(
            LeagueMatchRepository leagueMatchRepository,
            LineupRepository lineupRepository,
            MatchdayCalculationService matchdayCalculationService,
            Standing standing
    ) {
        this.leagueMatchRepository = leagueMatchRepository;
        this.lineupRepository = lineupRepository;
        this.matchdayCalculationService = matchdayCalculationService;
        this.standing = standing;
    }

    @Transactional
    public void calculateAndSaveResultsForMatchday(Long matchdayId) {
        List<LeagueMatch> matches = leagueMatchRepository.findAllByMatchdayIdAndHomeGoalsIsNull(matchdayId);
        for (LeagueMatch match : matches) {
            double homeScore = scoreFor(match, match.getHomeTeam().getId());
            double awayScore = scoreFor(match, match.getAwayTeam().getId());
            standing.updateStanding(match, homeScore, awayScore);
            leagueMatchRepository.save(match);
        }
    }

    // Una squadra che non ha schierato formazione per questa partita prende 0 fantapunti: non e'
    // un errore da bloccare (il proprietario semplicemente non ha giocato la giornata), quindi non
    // deve impedire il calcolo delle altre partite della stessa giornata.
    private double scoreFor(LeagueMatch match, Long teamId) {
        return lineupRepository.findByTeamIdAndLeagueMatchId(teamId, match.getId())
                .map(Lineup::getId)
                .map(matchdayCalculationService::calculateLineupScore)
                .orElse(0.0);
    }
}
