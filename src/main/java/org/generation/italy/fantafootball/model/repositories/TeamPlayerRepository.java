package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer,Long> {
    boolean existsByPlayer_IdAndLeague_IdAndTransferDateIsNull(Long playerId, Long leagueId);
}
