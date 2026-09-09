package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.InviteResponse;
import org.generation.italy.fantafootball.model.dto.UpdateInviteStatusRequest;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.services.LeagueInviteService;
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
    public InviteResponse updateInviteStatus(@PathVariable Long inviteId,
                                             @Valid @RequestBody UpdateInviteStatusRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        if (request.status() == LeagueInviteStatus.ACCEPTED) {
            return leagueInviteService.acceptInvite(inviteId, userId);
        }
        if (request.status() == LeagueInviteStatus.DECLINED) {
            return leagueInviteService.declineInvite(inviteId, userId);
        }
        throw new BadRequestException("INVALID_INVITE_STATUS",
                "Solo ACCEPTED o DECLINED sono supportati per gli inviti");
    }

    @GetMapping("/pending")
    public List<InviteResponse> getPendingInvites(@AuthenticationPrincipal Jwt jwt) {
        return leagueInviteService.getPendingInvitesForUser(extractUserId(jwt));
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}
