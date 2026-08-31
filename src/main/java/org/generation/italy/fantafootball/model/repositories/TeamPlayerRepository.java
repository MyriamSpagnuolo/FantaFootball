package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer,Long> {
    // 1. serve per trovare i giocatori nella squadra prima di rimuoverli
    List<TeamPlayer> findAllByTeamId(Long teamId);

    boolean existsByPlayer_IdAndLeague_IdAndTransferDateIsNull(Long playerId, Long leagueId);
}
