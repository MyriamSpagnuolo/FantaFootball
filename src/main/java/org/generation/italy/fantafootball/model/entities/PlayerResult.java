package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

// Un risultato e' associato al giocatore reale, condiviso da tutte le leghe.
@Entity
@Table(name = "player_results")
public class PlayerResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_results_id_seq_gen")
    @SequenceGenerator(
            name = "player_results_id_seq_gen",
            sequenceName = "seq_player_results_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(optional = false)
    @JoinColumn(name = "matchday_id", nullable = false)
    private Matchday matchday;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "goal_num", nullable = false)
    private int goalNum;

    @Column(name = "goal_conceded", nullable = false)
    private int goalConceded;

    @Column(name = "autogoal_num", nullable = false)
    private int autogoalNum;

    @Column(name = "assist_num", nullable = false)
    private int assistNum;

    @Column(name = "penalty_saved", nullable = false)
    private int penaltySaved;

    @Column(name = "penalty_failed", nullable = false)
    private int penaltyFailed;

    @Column(name = "clean_sheet")
    private Boolean cleanSheet;

    @Column(name = "yellow_card", nullable = false)
    private int yellowCard;

    @Column(name = "red_card", nullable = false)
    private boolean redCard;

    public PlayerResult() {
    }

    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Matchday getMatchday() {
        return matchday;
    }

    public void setMatchday(Matchday matchday) {
        this.matchday = matchday;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public int getGoalNum() {
        return goalNum;
    }

    public void setGoalNum(int goalNum) {
        this.goalNum = goalNum;
    }

    public int getGoalConceded() {
        return goalConceded;
    }

    public void setGoalConceded(int goalConceded) {
        this.goalConceded = goalConceded;
    }

    public int getAutogoalNum() {
        return autogoalNum;
    }

    public void setAutogoalNum(int autogoalNum) {
        this.autogoalNum = autogoalNum;
    }

    public int getAssistNum() {
        return assistNum;
    }

    public void setAssistNum(int assistNum) {
        this.assistNum = assistNum;
    }

    public int getPenaltySaved() {
        return penaltySaved;
    }

    public void setPenaltySaved(int penaltySaved) {
        this.penaltySaved = penaltySaved;
    }

    public int getPenaltyFailed() {
        return penaltyFailed;
    }

    public void setPenaltyFailed(int penaltyFailed) {
        this.penaltyFailed = penaltyFailed;
    }

    public Boolean getCleanSheet() {
        return cleanSheet;
    }

    public void setCleanSheet(Boolean cleanSheet) {
        this.cleanSheet = cleanSheet;
    }

    public int getYellowCard() {
        return yellowCard;
    }

    public void setYellowCard(int yellowCard) {
        this.yellowCard = yellowCard;
    }

    public boolean isRedCard() {
        return redCard;
    }

    public void setRedCard(boolean redCard) {
        this.redCard = redCard;
    }
}
