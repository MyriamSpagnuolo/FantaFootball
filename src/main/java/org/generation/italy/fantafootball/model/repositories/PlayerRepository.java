package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
