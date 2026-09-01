package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateInviteRequest;
import org.generation.italy.fantafootball.model.dto.InviteResponse;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.services.LeagueInviteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leagues/{leagueId}/invites")
public class LeagueInviteController {

    private final LeagueInviteService leagueInviteService;

    public LeagueInviteController(LeagueInviteService leagueInviteService) {
        this.leagueInviteService = leagueInviteService;
    }

    @PostMapping
    public ResponseEntity<?> sendInvite(@PathVariable Long leagueId,
                                        @Valid @RequestBody CreateInviteRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        try {
            Long invitedByUserId = extractUserId(jwt);
            InviteResponse response = leagueInviteService.sendInvite(leagueId, request, invitedByUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }
    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}
