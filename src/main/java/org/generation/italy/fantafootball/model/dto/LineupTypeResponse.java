package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.LineupType;

public record LineupTypeResponse(
        Long id,
        int defenderNum,
        int midfielderNum,
        int forwardNum
) {
    public static LineupTypeResponse fromEntity(LineupType lineupType) {
        return new LineupTypeResponse(
                lineupType.getId(),
                lineupType.getDefenderNum(),
                lineupType.getMidfielderNum(),
                lineupType.getForwardNum()
        );
    }
}
