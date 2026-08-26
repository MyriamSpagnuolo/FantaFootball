package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "team_player")
public class TeamPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_player_id_seq_gen")
    @SequenceGenerator(
            name = "team_player_id_seq_gen",
            sequenceName = "seq_team_player_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "real_team_name", nullable = false)
    private String realTeamName;

    @Column(name = "real_team_shirt_num", nullable = false)
    private int realTeamShirtNum;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "is_injured", nullable = false)
    private boolean injured;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "purchase_price", nullable = false)
    private int purchasePrice;

    public TeamPlayer() {
    }

    public TeamPlayer(Team team, String name, String surname, String realTeamName, int realTeamShirtNum,
                       int price, LocalDate purchaseDate, int purchasePrice) {
        this.team = team;
        this.name = name;
        this.surname = surname;
        this.realTeamName = realTeamName;
        this.realTeamShirtNum = realTeamShirtNum;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isInjured() {
        return injured;
    }

    public void setInjured(boolean injured) {
        this.injured = injured;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(int purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}
