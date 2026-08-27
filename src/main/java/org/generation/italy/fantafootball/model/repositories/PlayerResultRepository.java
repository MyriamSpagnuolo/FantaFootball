package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerResultRepository extends JpaRepository<PlayerResult, Long> {
    Optional<PlayerResult> findByMatchdayIdAndNameAndSurnameAndRealTeamNameAndRealTeamShirtNum(
            Long matchdayId,
            String name,
            String surname,
            String realTeamName,
            int realTeamShirtNum
    );
}
