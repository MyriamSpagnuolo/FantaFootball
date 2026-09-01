package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.InviteResponse;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.services.LeagueInviteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invites")
public class InviteResponseController {

    private final LeagueInviteService leagueInviteService;

    public InviteResponseController(LeagueInviteService leagueInviteService) {
        this.leagueInviteService = leagueInviteService;
    }

    @PatchMapping("/{inviteId}/accept")
    public ResponseEntity<?> acceptInvite(@PathVariable Long inviteId, @AuthenticationPrincipal Jwt jwt) {
        try {
            InviteResponse response = leagueInviteService.acceptInvite(inviteId, extractUserId(jwt));
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    @PatchMapping("/{inviteId}/decline")
    public ResponseEntity<?> declineInvite(@PathVariable Long inviteId, @AuthenticationPrincipal Jwt jwt) {
        try {
            InviteResponse response = leagueInviteService.declineInvite(inviteId, extractUserId(jwt));
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InviteResponse>> getPendingInvites(@AuthenticationPrincipal Jwt jwt) {
        List<InviteResponse> invites = leagueInviteService.getPendingInvitesForUser(extractUserId(jwt));
        return ResponseEntity.ok(invites);
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}
