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

    @ManyToOne(optional = false)
    @MapsId("playerId")
    @JoinColumn(name = "player_id", nullable = false)
    private TeamPlayer teamPlayer;

    @Column(name = "starter", nullable = false)
    private boolean starter = true;

    public LineupPlayer() {
    }

    public LineupPlayer(Lineup lineup, TeamPlayer teamPlayer, boolean starter) {
        this.lineup = lineup;
        this.teamPlayer = teamPlayer;
        this.starter = starter;
        this.id = new LineupPlayerId(lineup.getId(), teamPlayer.getId());
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

    public TeamPlayer getTeamPlayer() {
        return teamPlayer;
    }

    public void setTeamPlayer(TeamPlayer teamPlayer) {
        this.teamPlayer = teamPlayer;
        this.id.setPlayerId(teamPlayer != null ? teamPlayer.getId() : null);
    }

    public Long getPlayerId() {
        return id.getPlayerId();
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public PlayerRole getPosition() {
        return teamPlayer != null ? teamPlayer.getPlayer().getPosition() : null;
    }
}
