package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateTeamRequest;
import org.generation.italy.fantafootball.model.dto.TeamPlayerResponse;
import org.generation.italy.fantafootball.model.dto.TeamResponse;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.services.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        try {
            TeamResponse response = teamService.createTeam(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    @GetMapping("/{teamId}/players")
    public ResponseEntity<?> getTeamRoster(@PathVariable Long teamId) {
        try {
            List<TeamPlayerResponse> roster = teamService.getTeamRoster(teamId);
            return ResponseEntity.ok(roster);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{teamId}/players/{playerId}")
    public ResponseEntity<?> removePlayerFromTeam(@PathVariable Long teamId, @PathVariable Long playerId) {
        try {
            teamService.removePlayerFromTeam(teamId, playerId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }
}