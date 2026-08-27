package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lineup", uniqueConstraints = @UniqueConstraint(name = "uq_lineup", columnNames = {"team_id", "league_match_id"}))
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lineup_id_seq_gen")
    @SequenceGenerator(
            name = "lineup_id_seq_gen",
            sequenceName = "seq_lineup_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_match_id", nullable = false)
    private LeagueMatch leagueMatch;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lineup_type_id", nullable = false)
    private LineupType lineupType;

    @Column(name = "is_defensive", nullable = false)
    private boolean defensive;

    @OneToMany(mappedBy = "lineup", fetch = FetchType.LAZY)
    private List<LineupPlayer> players = new ArrayList<>();

    public Lineup() {
    }

    public Lineup(Team team, LeagueMatch leagueMatch, LineupType lineupType, boolean defensive) {
        this.team = team;
        this.leagueMatch = leagueMatch;
        this.lineupType = lineupType;
        this.defensive = defensive;
    }

    public Long getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public LeagueMatch getLeagueMatch() {
        return leagueMatch;
    }

    public void setLeagueMatch(LeagueMatch leagueMatch) {
        this.leagueMatch = leagueMatch;
    }

    public LineupType getLineupType() {
        return lineupType;
    }

    public void setLineupType(LineupType lineupType) {
        this.lineupType = lineupType;
    }

    public boolean isDefensive() {
        return defensive;
    }

    public void setDefensive(boolean defensive) {
        this.defensive = defensive;
    }

    public List<LineupPlayer> getPlayers() {
        return players;
    }

    public void addPlayer(LineupPlayer player) {
        players.add(player);
        player.setLineup(this);
    }

    public void removePlayer(LineupPlayer player) {
        players.remove(player);
        player.setLineup(null);
    }
}
