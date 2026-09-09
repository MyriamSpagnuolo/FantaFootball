package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateTradeRequest;
import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.services.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradeDto createTrade(@Valid @RequestBody CreateTradeRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        return tradeService.createTrade(request, getAuthenticatedUserId(jwt));
    }

    @GetMapping("/all")
    public List<TradeDto> getAllTradesByUserId(@AuthenticationPrincipal Jwt jwt) {
        return tradeService.getAllByUserId(getAuthenticatedUserId(jwt));
    }

    @GetMapping("/received/{teamId}")
    public List<TradeDto> getPendingReceivedTradeRequests(@PathVariable Long teamId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return tradeService.getAllPendingReceivedTradesByTeamId(teamId, getAuthenticatedUserId(jwt));
    }

    @GetMapping("/sent/{teamId}")
    public List<TradeDto> getPendingSentTradeRequests(@PathVariable Long teamId,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return tradeService.getAllPendingSentTradesByTeamId(teamId, getAuthenticatedUserId(jwt));
    }

    @GetMapping("/history/{teamId}")
    public List<TradeDto> getTradeHistory(@PathVariable Long teamId,
                                          @AuthenticationPrincipal Jwt jwt) {
        return tradeService.getTradeHistoryByTeamId(teamId, getAuthenticatedUserId(jwt));
    }

    @PatchMapping("/reject/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectTradeRequestById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        tradeService.rejectTradeById(id, getAuthenticatedUserId(jwt));
    }

    @PatchMapping("/accept/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptTradeRequestById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        tradeService.acceptTradeById(id, getAuthenticatedUserId(jwt));
    }

    private Long getAuthenticatedUserId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication is required");
        }
        Object claim = jwt.getClaim("uid");
        if (!(claim instanceof Number number)) {
            throw new org.springframework.security.access.AccessDeniedException("User ID is missing from token");
        }
        return number.longValue();
    }
}
