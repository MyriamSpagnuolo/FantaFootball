package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.calculateMatchday.MatchdayCalculationService;
import org.generation.italy.fantafootball.model.dto.TeamPlayerRatingResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/matches/{leagueMatchId}/players")
public class TeamMatchdayRatingController {

    private final MatchdayCalculationService matchdayCalculationService;

    public TeamMatchdayRatingController(MatchdayCalculationService matchdayCalculationService) {
        this.matchdayCalculationService = matchdayCalculationService;
    }

    @GetMapping("/ratings")
    public List<TeamPlayerRatingResponse> getRosterRatings(@PathVariable Long teamId,
                                                             @PathVariable Long leagueMatchId,
                                                             @AuthenticationPrincipal Jwt jwt) {
        return matchdayCalculationService.calculateTeamRosterRatings(teamId, leagueMatchId, extractUserId(jwt));
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }
}
