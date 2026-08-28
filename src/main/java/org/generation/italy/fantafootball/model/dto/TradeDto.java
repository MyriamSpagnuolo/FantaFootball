package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.Trade;
import org.generation.italy.fantafootball.model.entities.TradeStatus;

import java.time.LocalDateTime;

public record TradeDto(
        long id,
        long proposingTeamId,
        String proposingTeamName,
        long receivingTeamId,
        String receivingTeamName,
        String requestedPlayerName,
        String offeredPlayerName,
        Integer amount,
        TradeStatus status,
        LocalDateTime proposalDate
) {
    public static TradeDto fromEntity(Trade trade) {
        return new TradeDto(
                trade.getId(),
                trade.getProposingTeam().getId(),
                trade.getProposingTeam().getName(),
                trade.getReceivingTeam().getId(),
                trade.getReceivingTeam().getName(),
                trade.getRequestedPlayer().getName(),
                trade.getOfferedPlayer().getName(),
                trade.getAmount(),
                trade.getStatus(),
                trade.getProposalDate()
        );
    }
}
