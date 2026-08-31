package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.services.TradeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService){
        this.tradeService = tradeService;
    }


    @GetMapping("/all")
    public List<TradeDto> getAllTradesByUserId(@AuthenticationPrincipal Jwt jwt){
        Number userId = jwt.getClaim("uid");
        return tradeService.getAllByUserId(userId.longValue());
    }

    @GetMapping("/received/{teamId}")
    public List<TradeDto> getPendingReceivedTradeRequests(@PathVariable Long teamId) {
        return tradeService.getAllPendingReceivedTradesByTeamId(teamId);
    }

    @GetMapping("/sent/{teamId}")
    public List<TradeDto> getPendingSentTradeRequests(@PathVariable Long teamId) {
        return tradeService.getAllPendingSentTradesByTeamId(teamId);
    }

    @DeleteMapping("remove/{id}")
    public void removeTradeRequestById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("uid");
        tradeService.rejectTradeById(id, userId.longValue());
    }

    @PatchMapping("accept/{id}")
    public void acceptTradeRequestById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Number userId = jwt.getClaim("uid");
        tradeService.acceptTradeById(id, userId.longValue());
    }


}
