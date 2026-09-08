package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.LineupPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineupPlayerRepository extends JpaRepository<LineupPlayer, LineupPlayerId> {
    void deleteAllByLineup_Id(Long lineupId);
}
