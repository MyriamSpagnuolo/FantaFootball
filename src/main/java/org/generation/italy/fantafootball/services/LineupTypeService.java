package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.LineupTypeResponse;
import org.generation.italy.fantafootball.model.entities.Formation;
import org.generation.italy.fantafootball.model.entities.LineupType;
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

    // Seed idempotente dei 7 moduli standard (stessi id di seed-lineup-type.sql,
    // vedi Formation): inserisce solo quelli non ancora presenti, non tocca
    // righe già esistenti. Invocato allo startup da LineupTypeSeeder.
    @Transactional
    public void ensureStandardFormationsExist() {
        for (Formation formation : Formation.values()) {
            if (!lineupTypeRepository.existsById(formation.getId())) {
                lineupTypeRepository.save(new LineupType(
                        formation.getId(),
                        formation.getDefenderNum(),
                        formation.getMidfielderNum(),
                        formation.getForwardNum()
                ));
            }
        }
    }
}
