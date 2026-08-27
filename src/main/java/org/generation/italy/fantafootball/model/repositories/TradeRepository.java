package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByProposingTeam_Id(Long teamId);
    List<Trade> findByReceivingTeam_Id(Long teamId);

}
