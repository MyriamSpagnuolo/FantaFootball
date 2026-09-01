package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
    Optional<Player> findByNameAndSurnameAndRealTeamNameAndRealTeamShirtNum(
            String name, String surname, String realTeamName, int realTeamShirtNum);

    Optional<Player> findByExternalId(Long externalId);
}
