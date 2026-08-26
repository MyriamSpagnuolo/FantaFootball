package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "team", uniqueConstraints = @UniqueConstraint(name = "uq_team_name_league", columnNames = {"name", "league_id"}))
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_id_seq_gen")
    @SequenceGenerator(
            name = "team_id_seq_gen",
            sequenceName = "seq_team_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "budget", nullable = false)
    private int budget = 0;

    @Column(name = "total_points", nullable = false)
    private int totalPoints = 0;

    public Team() {
    }

    public Team(String name, AppUser user, League league) {
        this.name = name;
        this.user = user;
        this.league = league;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
