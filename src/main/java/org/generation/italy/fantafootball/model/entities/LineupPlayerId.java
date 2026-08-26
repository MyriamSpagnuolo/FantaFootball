package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LineupPlayerId implements Serializable {

    private Long lineupId;

    // No local "player" table; players are sourced externally.
    private Integer playerId;

    public LineupPlayerId() {
    }

    public LineupPlayerId(Long lineupId, Integer playerId) {
        this.lineupId = lineupId;
        this.playerId = playerId;
    }

    public Long getLineupId() {
        return lineupId;
    }

    public void setLineupId(Long lineupId) {
        this.lineupId = lineupId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineupPlayerId that)) return false;
        return Objects.equals(lineupId, that.lineupId) && Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineupId, playerId);
    }
}
