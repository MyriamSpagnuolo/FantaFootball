package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineupRepository extends JpaRepository<Lineup, Long> {
}
