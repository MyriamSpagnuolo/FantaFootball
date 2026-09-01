package org.generation.italy.fantafootball.controllers;

import jakarta.validation.Valid;
import org.generation.italy.fantafootball.model.dto.CreateTradeRequest;
import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.model.dto.UpdateTradeStatusRequest;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.generation.italy.fantafootball.services.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/trades")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeDto createTrade(@Valid @RequestBody CreateTradeRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        return tradeService.createTrade(request, getAuthenticatedUserId(jwt));
    }

    @GetMapping("/trades")
    public List<TradeDto> getAllTradesByUserId(@AuthenticationPrincipal Jwt jwt) {
        return tradeService.getAllByUserId(getAuthenticatedUserId(jwt));
    }

    @GetMapping("/teams/{teamId}/trades")
    public List<TradeDto> getTeamTrades(@PathVariable Long teamId,
                                        @RequestParam(required = false) String direction,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String scope,
                                        @AuthenticationPrincipal Jwt jwt) {
        Long userId = getAuthenticatedUserId(jwt);
        if ("history".equals(scope)) {
            return tradeService.getTradeHistoryByTeamId(teamId, userId);
        }
        if ("pending".equals(status) && "received".equals(direction)) {
            return tradeService.getAllPendingReceivedTradesByTeamId(teamId, userId);
        }
        if ("pending".equals(status) && "sent".equals(direction)) {
            return tradeService.getAllPendingSentTradesByTeamId(teamId, userId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trade filters");
    }

    @PatchMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTradeStatus(@PathVariable Long id,
                                  @Valid @RequestBody UpdateTradeStatusRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        Long userId = getAuthenticatedUserId(jwt);
        if (request.status() == TradeStatus.ACCEPTED) {
            tradeService.acceptTradeById(id, userId);
            return;
        }
        if (request.status() == TradeStatus.REJECTED) {
            tradeService.rejectTradeById(id, userId);
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Only ACCEPTED or REJECTED status updates are supported for trades");
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
