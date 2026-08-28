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

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "purchase_price", nullable = false)
    private int purchasePrice;

    public TeamPlayer() {
    }

    public TeamPlayer(Team team, League league, Player player, LocalDate purchaseDate, int purchasePrice) {
        this.team = team;
        this.league = league;
        this.player = player;
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

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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
