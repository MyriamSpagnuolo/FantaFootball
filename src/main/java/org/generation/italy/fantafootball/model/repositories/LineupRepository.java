package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineupRepository extends JpaRepository<Lineup, Long> {
    Optional<Lineup> findByTeamIdAndLeagueMatchId(Long teamId, Long leagueMatchId);

    boolean existsByTeamIdAndLeagueMatchId(Long teamId, Long leagueMatchId);
}
