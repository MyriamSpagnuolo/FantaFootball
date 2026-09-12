package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface PlayerResultRepository extends JpaRepository<PlayerResult, Long> {
    @EntityGraph(attributePaths = {"player", "matchday"})
    List<PlayerResult> findAllByPlayerIdIn(List<Long> playerIds);

    Optional<PlayerResult> findByPlayerIdAndMatchdayId(Long playerId, Long matchdayId);
}
