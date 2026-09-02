package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateLeagueRequest;
import org.generation.italy.fantafootball.model.dto.CreateTeamRequest;
import org.generation.italy.fantafootball.model.dto.LeagueResponse;
import org.generation.italy.fantafootball.model.dto.TeamStandingResponse;
import org.generation.italy.fantafootball.services.LeagueService;
import org.generation.italy.fantafootball.services.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeagueController {

    private final LeagueService leagueService;
    private final TeamService teamService;

    public LeagueController(LeagueService leagueService, TeamService teamService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
    }

    @PostMapping("/leagues")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public LeagueResponse createLeague(@Valid @RequestBody CreateLeagueRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        Long adminUserId = extractUserId(jwt);
        LeagueResponse leagueResponse = leagueService.createLeague(request, adminUserId);

        CreateTeamRequest teamRequest = new CreateTeamRequest(request.teamName(), leagueResponse.id());
        teamService.createTeam(teamRequest, adminUserId);

        return leagueResponse;
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }

    @GetMapping("/leagues/{leagueId}/teams")
    public List<TeamStandingResponse> getTeamsByLeague(@PathVariable Long leagueId, @AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("uid");
        return teamService.getTeamsByLeague(leagueId, userId.longValue());
    }
}
