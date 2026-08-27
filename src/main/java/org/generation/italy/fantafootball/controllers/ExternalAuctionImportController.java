package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.AuctionRosterImportRequest;
import org.generation.italy.fantafootball.services.ExternalAuctionImportService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/leagues/{leagueId}/teams/{teamId}")
public class ExternalAuctionImportController {
    private ExternalAuctionImportService service;

    public ExternalAuctionImportController(ExternalAuctionImportService service) {
        this.service = service;
    }

    @PostMapping("/players/import")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void importRoster(
            @PathVariable Long leagueId,
            @PathVariable Long teamId,
            @Valid @RequestBody AuctionRosterImportRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long authenticatedUserId = getAuthenticatedUserId(jwt);
         service.importRoster(
                 leagueId,
                 teamId,
                 authenticatedUserId,
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



