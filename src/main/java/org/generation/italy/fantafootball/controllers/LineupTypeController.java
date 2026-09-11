package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.LineupTypeResponse;
import org.generation.italy.fantafootball.services.LineupTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lineup-types")
public class LineupTypeController {

    private final LineupTypeService lineupTypeService;

    public LineupTypeController(LineupTypeService lineupTypeService) {
        this.lineupTypeService = lineupTypeService;
    }

    @GetMapping
    public List<LineupTypeResponse> getLineupTypes() {
        return lineupTypeService.getAllLineupTypes();
    }
}
