package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.LeagueMatchDto;
import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.services.LeagueMatchService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues/{leagueId}/matches")
public class LeagueMatchController {
    private final LeagueMatchService leagueMatchService;
    private final LeagueRepository leagueRepository;

    public LeagueMatchController(LeagueMatchService leagueMatchService, LeagueRepository leagueRepository) {
        this.leagueMatchService = leagueMatchService;
        this.leagueRepository = leagueRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<LeagueMatchDto> generateCalendar(@PathVariable Long leagueId, @AuthenticationPrincipal Jwt jwt) {
        Long currentUserId = jwt.getClaim("uid");
        if (!leagueRepository.existsByIdAndAdminId(leagueId, currentUserId)) {
            throw new AccessDeniedException("Solo l'admin della lega può generare il calendario");
        }

        List<LeagueMatch> matches = leagueMatchService.generateCalendar(leagueId);
        return matches.stream().map(LeagueMatchDto::from).toList();
    }
}
