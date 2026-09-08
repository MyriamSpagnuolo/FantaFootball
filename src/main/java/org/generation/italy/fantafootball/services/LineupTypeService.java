package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.LineupTypeResponse;
import org.generation.italy.fantafootball.model.repositories.LineupTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LineupTypeService {

    private final LineupTypeRepository lineupTypeRepository;

    public LineupTypeService(LineupTypeRepository lineupTypeRepository) {
        this.lineupTypeRepository = lineupTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<LineupTypeResponse> getAllLineupTypes() {
        return lineupTypeRepository.findAll().stream()
                .map(LineupTypeResponse::fromEntity)
                .toList();
    }
}
