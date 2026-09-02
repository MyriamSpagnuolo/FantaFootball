package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.calculateMatchday.MatchdayCalculationService;
import org.generation.italy.fantafootball.model.dto.PlayerFilterRequest;
import org.generation.italy.fantafootball.model.dto.PlayerResponse;
import org.generation.italy.fantafootball.services.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final MatchdayCalculationService matchdayCalculationService;

    public PlayerController(PlayerService playerService, MatchdayCalculationService matchdayCalculationService) {
        this.playerService = playerService;
        this.matchdayCalculationService = matchdayCalculationService;
    }

    @GetMapping
    public List<PlayerResponse> getPlayers(
            @ModelAttribute PlayerFilterRequest filters) {

        return playerService.findPlayers(filters);
    }

    @GetMapping("/{playerId}/matchdays/{matchdayId}/rating")
    public PlayerRatingResponse getPlayerRating(@PathVariable Long playerId, @PathVariable Long matchdayId) {
        double fantaRating = matchdayCalculationService.calculatePlayerRating(playerId, matchdayId);
        return new PlayerRatingResponse(fantaRating);
    }

    public record PlayerRatingResponse(double fantaRating) {
    }
}
