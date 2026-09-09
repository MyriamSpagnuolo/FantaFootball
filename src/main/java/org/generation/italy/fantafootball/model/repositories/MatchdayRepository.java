package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Matchday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchdayRepository extends JpaRepository<Matchday, Long> {
    List<Matchday> findByClosedFalseOrderByDateAsc();

    Optional<Matchday> findByNumber(int number);
}
