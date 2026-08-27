package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;

import java.time.LocalDateTime;

public record TradeDto(
        long id,
        String otherTeamId,
        String otherTeamName,
        long requestedPlayerId,
        long offeredPlayerId,
        Integer amount,
        TradeStatus status,
        LocalDateTime proposalDate
) {
    public static TradeDto fromEntity(Trade trade, String otherTeamName) {
        return new TradeDto(
                trade.getId(),
                otherTeamName,
                trade.getOfferedPlayer().getId(),
                trade.getAmount(),
                trade.getStatus(),
                trade.getProposalDate()
        );
    }
}
