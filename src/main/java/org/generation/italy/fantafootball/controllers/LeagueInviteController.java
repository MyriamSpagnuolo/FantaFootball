package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateInviteRequest;
import org.generation.italy.fantafootball.model.dto.InviteResponse;
import org.generation.italy.fantafootball.services.LeagueInviteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leagues/{leagueId}/invites")
public class LeagueInviteController {

    private final LeagueInviteService leagueInviteService;

    public LeagueInviteController(LeagueInviteService leagueInviteService) {
        this.leagueInviteService = leagueInviteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse sendInvite(@PathVariable Long leagueId,
                                     @Valid @RequestBody CreateInviteRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        Long invitedByUserId = extractUserId(jwt);
        return leagueInviteService.sendInvite(leagueId, request, invitedByUserId);
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}
