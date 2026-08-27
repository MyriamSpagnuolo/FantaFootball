package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Matchday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchdayRepository extends JpaRepository<Matchday, Long> {
    List<Matchday> findByClosedFalseOrderByDateAsc();
}
