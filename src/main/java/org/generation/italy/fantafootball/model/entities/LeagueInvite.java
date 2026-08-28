package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "league_invite")
public class LeagueInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "league_invite_id_seq_gen")
    @SequenceGenerator(name = "league_invite_id_seq_gen", sequenceName = "seq_league_invite_id", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "league_id", nullable = false)
    private League league;
    @ManyToOne(optional = false) @JoinColumn(name = "invited_by_user_id", nullable = false)
    private AppUser invitedBy;
    @ManyToOne(optional = false) @JoinColumn(name = "invited_user_id", nullable = false)
    private AppUser invitedUser;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private LeagueInviteStatus status;
    @Column(name = "sent_date", nullable = false) private LocalDateTime sentDate;
    @Column(name = "response_date") private LocalDateTime responseDate;

    public LeagueInvite() {}

    public LeagueInvite(League league, AppUser invitedBy, AppUser invitedUser,
                        LeagueInviteStatus status) {
        this.league = league;
        this.invitedBy = invitedBy;
        this.invitedUser = invitedUser;
        this.status = status;
    }

    public Long getId() { return id; }
    public League getLeague() { return league; }
    public void setLeague(League league) { this.league = league; }
    public AppUser getInvitedBy() { return invitedBy; }
    public void setInvitedBy(AppUser invitedBy) { this.invitedBy = invitedBy; }
    public AppUser getInvitedUser() { return invitedUser; }
    public void setInvitedUser(AppUser invitedUser) { this.invitedUser = invitedUser; }
    public LeagueInviteStatus getStatus() { return status; }
    public void setStatus(LeagueInviteStatus status) { this.status = status; }
    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
    public LocalDateTime getResponseDate() { return responseDate; }
    public void setResponseDate(LocalDateTime responseDate) { this.responseDate = responseDate; }
}
