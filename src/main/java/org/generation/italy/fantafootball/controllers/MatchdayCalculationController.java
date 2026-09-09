package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.calculateMatchday.MatchdayCalculationService;
import org.generation.italy.fantafootball.calculateMatchday.GoalsCalculator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lineups")
public class MatchdayCalculationController {
    private final MatchdayCalculationService calculationService;

    public MatchdayCalculationController(MatchdayCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @GetMapping("/{lineupId}/score")
    public LineupCalculationResponse calculate(@PathVariable Long lineupId) {
        double score = calculationService.calculateLineupScore(lineupId);
        return new LineupCalculationResponse(score, GoalsCalculator.calculateGoals(score));
    }

    public record LineupCalculationResponse(double score, int goals) {
    }
}
