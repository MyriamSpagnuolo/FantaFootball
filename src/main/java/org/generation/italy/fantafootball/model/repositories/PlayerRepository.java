package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByNameAndSurnameAndRealTeamNameAndRealTeamShirtNum(
            String name, String surname, String realTeamName, int realTeamShirtNum);
}
