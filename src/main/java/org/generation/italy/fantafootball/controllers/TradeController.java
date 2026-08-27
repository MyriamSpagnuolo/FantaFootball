package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.services.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/trades")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService){
        this.tradeService = tradeService;
    }

    @GetMapping("/received")
    public List<TradeDto> getReceivedTradeRequests(@PathVariable Long teamId) {
        return tradeService.getAllReceivedTradesByTeamId(teamId);
    }

    @GetMapping("/sent")
    public List<TradeDto> getSentTradeRequests(@PathVariable  Long teamId) {
        return tradeService.getAllSentTradesByTeamId(teamId);
    }

    @DeleteMapping("remove/{id}")
    public void removeTradeRequestById(@PathVariable Long id) {
        tradeService.deleteTradeById(id);
    }

}
