package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "league_match")
public class LeagueMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "league_match_id_seq_gen")
    @SequenceGenerator(
            name = "league_match_id_seq_gen",
            sequenceName = "seq_league_match_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "match_day", nullable = false)
    private LocalDateTime matchDay;

    // Giornata reale di campionato che fornisce i player_results per calcolare questo scontro.
    @ManyToOne(optional = false)
    @JoinColumn(name = "matchday_id", nullable = false)
    private Matchday matchday;

    // Giornata relativa alla lega (parte da 1 alla nascita della lega, indipendente da matchday.number).
    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "home_score", precision = 5, scale = 2)
    private BigDecimal homeScore;

    @Column(name = "away_score", precision = 5, scale = 2)
    private BigDecimal awayScore;

    @Column(name = "home_goals")
    private Integer homeGoals;

    @Column(name = "away_goals")
    private Integer awayGoals;

    public LeagueMatch() {
    }

    public LeagueMatch(League league, LocalDateTime matchDay, Team homeTeam, Team awayTeam, Matchday matchday, int roundNumber) {
        this.league = league;
        this.matchDay = matchDay;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchday = matchday;
        this.roundNumber = roundNumber;
    }

    public Long getId() {
        return id;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public LocalDateTime getMatchDay() {
        return matchDay;
    }

    public void setMatchDay(LocalDateTime matchDay) {
        this.matchDay = matchDay;
    }

    public Matchday getMatchday() {
        return matchday;
    }

    public void setMatchday(Matchday matchday) {
        this.matchday = matchday;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public BigDecimal getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(BigDecimal homeScore) {
        this.homeScore = homeScore;
    }

    public BigDecimal getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(BigDecimal awayScore) {
        this.awayScore = awayScore;
    }

    public Integer getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(Integer homeGoals) {
        this.homeGoals = homeGoals;
    }

    public Integer getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(Integer awayGoals) {
        this.awayGoals = awayGoals;
    }
}
