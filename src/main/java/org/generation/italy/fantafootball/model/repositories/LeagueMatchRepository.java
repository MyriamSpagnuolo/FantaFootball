package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueMatchRepository extends JpaRepository<LeagueMatch, Long> {
    boolean existsByLeagueId(Long leagueId);

    List<LeagueMatch> findAllByLeagueIdOrderByRoundNumberAsc(Long leagueId);

    // Solo i league_match ancora senza risultato: usato da LeagueMatchScoreService per il
    // ricalcolo idempotente (home_goals e' valorizzato in un solo colpo insieme ad away_goals
    // da Standing.updateStanding, quindi e' un marcatore affidabile di "gia' calcolato").
    List<LeagueMatch> findAllByMatchdayIdAndHomeGoalsIsNull(Long matchdayId);
}
