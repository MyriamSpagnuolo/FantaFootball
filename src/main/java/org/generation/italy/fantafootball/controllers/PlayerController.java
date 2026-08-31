package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.PlayerFilterRequest;
import org.generation.italy.fantafootball.model.dto.PlayerResponse;
import org.generation.italy.fantafootball.services.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<PlayerResponse> getPlayers(
            @ModelAttribute PlayerFilterRequest filters) {

        return playerService.findPlayers(filters);
    }
}
