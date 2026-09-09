package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.services.ExternalAuctionImportService;
import org.generation.italy.fantafootball.model.dto.PurchasePlayerRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leagues/{leagueId}/teams/{teamId}")
public class AuctionImportController {
    private final ExternalAuctionImportService importService;

    public AuctionImportController(ExternalAuctionImportService service) {
        this.importService = service;
    }

    @PostMapping("/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void importPlayer(
            @PathVariable Long leagueId,
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @Valid @RequestBody PurchasePlayerRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long authenticatedUserId = getAuthenticatedUserId(jwt);
         importService.importPlayer(
                 leagueId,
                 teamId,
                 authenticatedUserId,
                 playerId,
                 request
         );
    }

    private Long getAuthenticatedUserId(Jwt jwt) {
        Object claim = jwt.getClaim("uid");
        if (!(claim instanceof Number number)) {
            throw new AccessDeniedException("ID utente non presente nel token");
        }
        return number.longValue();
    }
}



