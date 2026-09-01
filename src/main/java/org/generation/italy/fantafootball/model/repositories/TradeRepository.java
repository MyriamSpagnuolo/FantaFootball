package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByProposingTeam_IdAndStatus(Long teamId, TradeStatus status);
    List<Trade> findByReceivingTeam_IdAndStatus(Long teamId, TradeStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trade t where t.id = :tradeId")
    java.util.Optional<Trade> findByIdForUpdate(@Param("tradeId") Long tradeId);

    @Modifying(flushAutomatically = true)
    @Query("""
      UPDATE Trade t
      SET t.status = org.generation.italy.fantafootball.model.entities.TradeStatus.CANCELLED
      WHERE t.status = org.generation.italy.fantafootball.model.entities.TradeStatus.PENDING
        AND t.id <> :acceptedTradeId
        AND (t.requestedPlayer.id IN :playerIds OR t.offeredPlayer.id IN :playerIds)
      """)
    int cancelPendingTradesInvolvingPlayers(@Param("playerIds") Collection<Long> playerIds,
                                            @Param("acceptedTradeId") Long acceptedTradeId);

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
