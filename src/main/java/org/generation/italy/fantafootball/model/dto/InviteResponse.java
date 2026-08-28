package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.LeagueInvite;

import java.time.LocalDateTime;

public record InviteResponse(
        Long id, Long leagueId, Long invitedByUserId, Long invitedUserId,
        String status, LocalDateTime sentDate, LocalDateTime responseDate
) {
    public static InviteResponse fromEntity(LeagueInvite invite) {
        return new InviteResponse(
                invite.getId(),
                invite.getLeague().getId(),
                invite.getInvitedBy().getId(),
                invite.getInvitedUser().getId(),
                invite.getStatus().name(),
                invite.getSentDate(),
                invite.getResponseDate()
        );
    }
}
