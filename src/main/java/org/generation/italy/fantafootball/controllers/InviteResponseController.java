package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.InviteResponse;
import org.generation.italy.fantafootball.model.dto.RespondToInviteRequest;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.services.LeagueInviteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> acceptInvite(@PathVariable Long inviteId,
                                          @Valid @RequestBody RespondToInviteRequest request) {
        try {
            InviteResponse response = leagueInviteService.acceptInvite(inviteId, request);
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
    public ResponseEntity<?> declineInvite(@PathVariable Long inviteId,
                                           @Valid @RequestBody RespondToInviteRequest request) {
        try {
            InviteResponse response = leagueInviteService.declineInvite(inviteId, request);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    @GetMapping("/pending/user/{userId}")
    public ResponseEntity<List<InviteResponse>> getPendingInvites(@PathVariable Long userId) {
        List<InviteResponse> invites = leagueInviteService.getPendingInvitesForUser(userId);
        return ResponseEntity.ok(invites);
    }
}
