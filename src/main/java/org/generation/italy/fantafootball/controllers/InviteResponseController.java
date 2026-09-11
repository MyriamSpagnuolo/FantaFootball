package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.InviteResponse;
import org.generation.italy.fantafootball.model.dto.UpdateInviteStatusRequest;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.services.LeagueInviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
public class InviteResponseController {

    private final LeagueInviteService leagueInviteService;

    public InviteResponseController(LeagueInviteService leagueInviteService) {
        this.leagueInviteService = leagueInviteService;
    }

    @PatchMapping("/{inviteId}")
    public ResponseEntity<InviteResponse> updateInviteStatus(@PathVariable Long inviteId,
                                                             @Valid @RequestBody UpdateInviteStatusRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        if (request.status() == LeagueInviteStatus.ACCEPTED) {
            return ResponseEntity.ok(leagueInviteService.acceptInvite(inviteId, userId));
        }
        if (request.status() == LeagueInviteStatus.DECLINED) {
            return ResponseEntity.ok(leagueInviteService.declineInvite(inviteId, userId));
        }
        if (request.status() == LeagueInviteStatus.CANCELLED) {
            leagueInviteService.cancelInvite(inviteId, userId);
            return ResponseEntity.noContent().build();
        }
        throw new BadRequestException("INVALID_INVITE_STATUS",
                "Solo ACCEPTED, DECLINED o CANCELLED sono supportati per gli inviti");
    }

    @GetMapping({"/pending", "/received"})
    public List<InviteResponse> getPendingInvites(@AuthenticationPrincipal Jwt jwt) {
        return leagueInviteService.getPendingInvitesForUser(extractUserId(jwt));
    }

    @GetMapping("/sent")
    public List<InviteResponse> getSentInvites(@AuthenticationPrincipal Jwt jwt) {
        return leagueInviteService.getSentInvitesForUser(extractUserId(jwt));
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}
