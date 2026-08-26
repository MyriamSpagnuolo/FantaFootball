package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_id_seq_gen")
    @SequenceGenerator(
            name = "trade_id_seq_gen",
            sequenceName = "seq_trade_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proposing_team_id", nullable = false)
    private Team proposingTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiving_team_id", nullable = false)
    private Team receivingTeam;

    // The player owned by the receiving team that the proposing team wants.
    @ManyToOne(optional = false)
    @JoinColumn(name = "trade_player_id", nullable = false)
    private TeamPlayer requestedPlayer;

    // The player owned by the proposing team, offered in exchange.
    @ManyToOne(optional = false)
    @JoinColumn(name = "offered_player_id", nullable = false)
    private TeamPlayer offeredPlayer;

    // Optional cash adjustment on top of the player swap.
    // Positive: extra cash offered by the proposing team. Negative: cash requested from the receiving team.
    @Column(name = "amount")
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TradeStatus status;

    @Column(name = "proposal_date", nullable = false)
    private LocalDateTime proposalDate;

    public Trade() {
    }

    public Trade(Team proposingTeam, Team receivingTeam, TeamPlayer requestedPlayer, TeamPlayer offeredPlayer, TradeStatus status) {
        this.proposingTeam = proposingTeam;
        this.receivingTeam = receivingTeam;
        this.requestedPlayer = requestedPlayer;
        this.offeredPlayer = offeredPlayer;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Team getProposingTeam() {
        return proposingTeam;
    }

    public void setProposingTeam(Team proposingTeam) {
        this.proposingTeam = proposingTeam;
    }

    public Team getReceivingTeam() {
        return receivingTeam;
    }

    public void setReceivingTeam(Team receivingTeam) {
        this.receivingTeam = receivingTeam;
    }

    public TeamPlayer getRequestedPlayer() {
        return requestedPlayer;
    }

    public void setRequestedPlayer(TeamPlayer requestedPlayer) {
        this.requestedPlayer = requestedPlayer;
    }

    public TeamPlayer getOfferedPlayer() {
        return offeredPlayer;
    }

    public void setOfferedPlayer(TeamPlayer offeredPlayer) {
        this.offeredPlayer = offeredPlayer;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    public LocalDateTime getProposalDate() {
        return proposalDate;
    }

    public void setProposalDate(LocalDateTime proposalDate) {
        this.proposalDate = proposalDate;
    }
}
