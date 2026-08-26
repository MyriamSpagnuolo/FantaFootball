package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "lineup_player")
public class LineupPlayer {

    @EmbeddedId
    private LineupPlayerId id = new LineupPlayerId();

    @ManyToOne(optional = false)
    @MapsId("lineupId")
    @JoinColumn(name = "lineup_id", nullable = false)
    private Lineup lineup;

    @Column(name = "starter", nullable = false)
    private boolean starter = true;

    @Column(name = "position", length = 20)
    private String position;

    public LineupPlayer() {
    }

    public LineupPlayer(Lineup lineup, Integer playerId, boolean starter) {
        this.lineup = lineup;
        this.starter = starter;
        this.id = new LineupPlayerId(lineup.getId(), playerId);
    }

    public LineupPlayerId getId() {
        return id;
    }

    public Lineup getLineup() {
        return lineup;
    }

    public void setLineup(Lineup lineup) {
        this.lineup = lineup;
        this.id.setLineupId(lineup != null ? lineup.getId() : null);
    }

    public Integer getPlayerId() {
        return id.getPlayerId();
    }

    public void setPlayerId(Integer playerId) {
        this.id.setPlayerId(playerId);
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
