package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

// name/surname/real_team_name/real_team_shirt_num identificano il giocatore reale e sono
// vincolati da FK composita a team_player (stessa quadrupla, unica su team_player).
// Nessuna @ManyToOne verso TeamPlayer: qui teniamo solo le colonne scalari corrispondenti,
// senza aggiungere una relazione objectuale finche' non serve davvero.
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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "real_team_name", nullable = false)
    private String realTeamName;

    @Column(name = "real_team_shirt_num", nullable = false)
    private int realTeamShirtNum;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRealTeamName() {
        return realTeamName;
    }

    public void setRealTeamName(String realTeamName) {
        this.realTeamName = realTeamName;
    }

    public int getRealTeamShirtNum() {
        return realTeamShirtNum;
    }

    public void setRealTeamShirtNum(int realTeamShirtNum) {
        this.realTeamShirtNum = realTeamShirtNum;
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
