package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.calculateMatchday.MatchdayCalculationService;
import org.generation.italy.fantafootball.model.dto.PlayerFilterRequest;
import org.generation.italy.fantafootball.model.dto.PlayerResponse;
import org.generation.italy.fantafootball.model.dto.PageResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.generation.italy.fantafootball.services.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.generation.italy.fantafootball.model.dto.PriceRangeResponse;
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
    public PageResponse<PlayerResponse> getPlayers(
            @ModelAttribute PlayerFilterRequest filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return playerService.findPlayers(filters, page, size);
    }

    @GetMapping("/real-teams")
    public List<String> getRealTeams() { return playerService.findRealTeams(); }

    @GetMapping("/price-range")
    public PriceRangeResponse getPriceRange(@ModelAttribute PlayerFilterRequest filters) {
        return playerService.findPriceRange(filters);
    }

    @GetMapping("/{playerId}/matchdays/{matchdayId}/rating")
    public PlayerRatingResponse getPlayerRating(@PathVariable Long playerId, @PathVariable Long matchdayId) {
        double fantaRating = matchdayCalculationService.calculatePlayerRating(playerId, matchdayId);
        return new PlayerRatingResponse(fantaRating);
    }

    public record PlayerRatingResponse(double fantaRating) {
    }
}
