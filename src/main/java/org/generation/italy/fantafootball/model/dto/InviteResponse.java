package org.generation.italy.fantafootball.model.dto;

import org.generation.italy.fantafootball.model.entities.LeagueInvite;

import java.time.LocalDateTime;

public record InviteResponse(
        Long id, Long leagueId, Long invitedByUserId, Long invitedUserId,
        String status, LocalDateTime sentDate, LocalDateTime responseDate
) {
    /*
     * Converte un'Entity LeagueInvite (con le sue relazioni JPA verso League e AppUser)
     * nel DTO di risposta da restituire al client. Estrae solo gli id delle entità collegate
     * (leagueId, invitedByUserId, invitedUserId) invece di esporre gli oggetti interi,
     * e trasforma l'enum LeagueInviteStatus in stringa (es. "PENDING") per la serializzazione JSON.
     */
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