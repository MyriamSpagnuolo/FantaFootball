package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.PlayerResultDto;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.services.PlayerResultService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Un controller REST riceve richieste HTTP (per esempio: POST /api/player-results) e restituisce una risposta HTTP.
@RestController // Con questa annotazione diciamo a Spring che la classe è un controller REST
@RequestMapping("/api/player-results") // Questa annotazione definisce il percorso base del controller
public class PlayerResultController {
    private final PlayerResultService playerResultService;

    // dependency constructor injection
    public PlayerResultController(PlayerResultService playerResultService) {
        this.playerResultService = playerResultService;
    }

    // L'annotazione "RequestBody" dice a Spring di prendere il contenuto JSON del body della richiesta HTTP
    // e convertilo in un oggetto Java (Spring crea PlayerResultDto request).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResult create(@RequestBody PlayerResultDto request) {
        return playerResultService.create(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<PlayerResult> createAll(
            @RequestBody List<PlayerResultDto> requests) {

        return playerResultService.createAll(requests);
    }
}
