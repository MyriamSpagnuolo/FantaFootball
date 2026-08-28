package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByProposingTeam_IdAndStatus(Long teamId, TradeStatus status);
    List<Trade> findByReceivingTeam_IdAndStatus(Long teamId, TradeStatus status);

    @Query("""
      SELECT t
      FROM Trade t
      WHERE (t.proposingTeam.id = :teamId
             OR t.receivingTeam.id = :teamId)
        AND t.status <> org.generation.italy.fantafootball.model.entities.TradeStatus.PENDING
      ORDER BY t.proposalDate DESC
      """)
    List<Trade> findTradeHistoryByTeamId(@Param("teamId") Long teamId);

    @Query("""
      SELECT t
      FROM Trade t
      WHERE t.proposingTeam.user.id = :userId
         OR t.receivingTeam.user.id = :userId
      ORDER BY t.proposalDate DESC
      """)
    List<Trade> findAllByUserId(@Param("userId") Long userId);

}
