package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByNameAndLeagueId(String name, Long leagueId);
    boolean existsByUserIdAndLeagueId(Long userId, Long leagueId);
    List<Team> findAllTeamByLeagueId(Long leagueId);
    List<Team> findAllByUserId(Long userId);
    long countByLeagueId(Long leagueId);

    Optional<Team> findByIdAndUserId(Long teamId, Long userId);

    Optional<Team> findFirstByUserIdAndLeagueId(Long userId, Long leagueId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from Team t where t.id = :teamId")
    Optional<Team> findByIdForUpdate(@org.springframework.data.repository.query.Param("teamId") Long teamId);
}
