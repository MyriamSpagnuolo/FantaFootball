package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateTeamRequest;
import org.generation.italy.fantafootball.model.dto.RenameTeamRequest;
import org.generation.italy.fantafootball.model.dto.TeamPlayerResponse;
import org.generation.italy.fantafootball.model.dto.TeamResponse;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.services.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        try {
            Long userId = extractUserId(jwt);
            TeamResponse response = teamService.createTeam(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyTeams(@AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("uid");
        List<TeamResponse> teams = teamService.getMyTeams(userId.longValue());
        return ResponseEntity.ok(teams);
    }

    @PatchMapping("/{teamId}/name")
    public ResponseEntity<?> renameTeam(@PathVariable Long teamId,
                                         @Valid @RequestBody RenameTeamRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        try {
            Number userId = jwt.getClaim("uid");
            TeamResponse response = teamService.renameTeam(teamId, userId.longValue(), request);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        return uid.longValue();
    }

    @GetMapping("/{teamId}/players")
    public ResponseEntity<?> getTeamRoster(@PathVariable Long teamId, @AuthenticationPrincipal Jwt jwt) {
        try {
            List<TeamPlayerResponse> roster = teamService.getTeamRoster(teamId, extractUserId(jwt));
            return ResponseEntity.ok(roster);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorCode", e.getErrorCode(), "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{teamId}/players/{playerId}")
    public ResponseEntity<?> removePlayerFromTeam(@PathVariable Long teamId, @PathVariable Long playerId,
                                                  @AuthenticationPrincipal Jwt jwt) {
        try {
            teamService.removePlayerFromTeam(teamId, playerId, extractUserId(jwt));
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