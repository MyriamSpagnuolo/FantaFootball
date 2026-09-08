package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.LineupRequest;
import org.generation.italy.fantafootball.model.dto.LineupResponse;
import org.generation.italy.fantafootball.services.LineupService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/{teamId}/matches/{leagueMatchId}/lineup")
public class LineupController {

    private final LineupService lineupService;

    public LineupController(LineupService lineupService) {
        this.lineupService = lineupService;
    }

    @GetMapping
    public LineupResponse getLineup(@PathVariable Long teamId, @PathVariable Long leagueMatchId,
                                     @AuthenticationPrincipal Jwt jwt) {
        return lineupService.getLineup(teamId, leagueMatchId, extractUserId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LineupResponse createLineup(@PathVariable Long teamId, @PathVariable Long leagueMatchId,
                                        @Valid @RequestBody LineupRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return lineupService.createLineup(teamId, leagueMatchId, request, extractUserId(jwt));
    }

    @PutMapping
    public LineupResponse updateLineup(@PathVariable Long teamId, @PathVariable Long leagueMatchId,
                                        @Valid @RequestBody LineupRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return lineupService.updateLineup(teamId, leagueMatchId, request, extractUserId(jwt));
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}
