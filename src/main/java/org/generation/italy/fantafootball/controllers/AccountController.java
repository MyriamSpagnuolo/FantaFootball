package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.DisableAccountRequest;
import org.generation.italy.fantafootball.model.dto.UserLeagueTeamResponse;
import org.generation.italy.fantafootball.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableAccount(
            @Valid @RequestBody DisableAccountRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        accountService.disableAccount(getAuthenticatedUserId(jwt), request.currentPassword());
    }

    @GetMapping("/me/leagues")
    public List<UserLeagueTeamResponse> getLeaguesAndTeams(@AuthenticationPrincipal Jwt jwt) {
        return accountService.getLeaguesAndTeams(getAuthenticatedUserId(jwt));
    }

    private Long getAuthenticatedUserId(Jwt jwt) {
        Object claim = jwt.getClaim("uid");
        if (!(claim instanceof Number userId)) {
            throw new AccessDeniedException("ID utente non presente nel token");
        }
        return userId.longValue();
    }
}
