package org.generation.italy.fantafootball.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LineupTypeSeeder implements CommandLineRunner {

    private final LineupTypeService lineupTypeService;

    public LineupTypeSeeder(LineupTypeService lineupTypeService) {
        this.lineupTypeService = lineupTypeService;
    }

    @Override
    public void run(String... args) {
        lineupTypeService.ensureStandardFormationsExist();
    }
}
