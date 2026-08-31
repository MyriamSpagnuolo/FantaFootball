package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByNameAndLeagueId(String name, Long leagueId);
    boolean existsByUserIdAndLeagueId(Long userId, Long leagueId);
    List<Team> findAllTeamByLeagueId(Long leagueId);
    long countByLeagueId(Long leagueId);
}
