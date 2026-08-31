package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.League;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<League, Long> {
    boolean existsByInviteCode(String inviteCode);
    boolean existsByIdAndAdminId(Long id, Long adminId);
}
