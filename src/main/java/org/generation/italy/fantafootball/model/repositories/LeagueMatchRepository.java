package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueMatchRepository extends JpaRepository<LeagueMatch, Long> {
    boolean existsByLeagueId(Long leagueId);

    List<LeagueMatch> findAllByLeagueIdOrderByRoundNumberAsc(Long leagueId);
}
