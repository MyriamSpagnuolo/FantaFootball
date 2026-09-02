package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateTeamRequest;
import org.generation.italy.fantafootball.model.dto.RenameTeamRequest;
import org.generation.italy.fantafootball.model.dto.TeamPlayerResponse;
import org.generation.italy.fantafootball.model.dto.TeamResponse;
import org.generation.italy.fantafootball.services.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@Valid @RequestBody CreateTeamRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        return teamService.createTeam(request, userId);
    }

    @GetMapping("/me")
    public List<TeamResponse> getMyTeams(@AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("uid");
        return teamService.getMyTeams(userId.longValue());
    }

    @PatchMapping("/{teamId}")
    public TeamResponse renameTeam(@PathVariable Long teamId,
                                   @Valid @RequestBody RenameTeamRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("uid");
        return teamService.renameTeam(teamId, userId.longValue(), request);
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }

    @GetMapping("/{teamId}/players")
    public List<TeamPlayerResponse> getTeamRoster(@PathVariable Long teamId, @AuthenticationPrincipal Jwt jwt) {
        return teamService.getTeamRoster(teamId, extractUserId(jwt));
    }

    @DeleteMapping("/{teamId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromTeam(@PathVariable Long teamId, @PathVariable Long playerId,
                                     @AuthenticationPrincipal Jwt jwt) {
        teamService.removePlayerFromTeam(teamId, playerId, extractUserId(jwt));
    }
}
