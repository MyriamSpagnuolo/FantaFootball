package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.PlayerResultDto;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerResultService {
    private final PlayerResultRepository playerResultRepository;

    public PlayerResultService(PlayerResultRepository playerResultRepository) {
        this.playerResultRepository = playerResultRepository;
    }

    @Transactional
    public PlayerResult create(PlayerResultDto request) {

        // creiamo il "result" che sarebbe l'entity che Hibernate potrà salvare
        PlayerResult result = new PlayerResult();

        // Poi qui facciamo il mapping per tutti i campi:
        result.setName(request.name()); // request.name()  ───→  result.name
        result.setSurname(request.surname());
        result.setRealTeamName(request.realTeamName());
        result.setRealTeamShirtNum(request.realTeamShirtNum());
        result.setRating(request.rating());
        result.setGoalNum(request.goalNum());
        result.setGoalConceded(request.goalConceded());
        result.setAutogoalNum(request.autogoalNum());
        result.setAssistNum(request.assistNum());
        result.setPenaltySaved(request.penaltySaved());
        result.setPenaltyFailed(request.penaltyFailed());
        result.setCleanSheet(request.cleanSheet());
        result.setYellowCard(request.yellowCard());
        result.setRedCard(request.redCard());

        // Spring Data JPA salva l'entity nella tabella player_results
        return playerResultRepository.save(result);
    }
}
