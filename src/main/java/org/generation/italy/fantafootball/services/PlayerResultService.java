package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.PlayerResultDto;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerResultService {

    private final PlayerResultRepository playerResultRepository;

    public PlayerResultService(PlayerResultRepository playerResultRepository) {
        this.playerResultRepository = playerResultRepository;
    }

    @Transactional
    public PlayerResult create(PlayerResultDto request) {

        PlayerResult result = toEntity(request);

        return playerResultRepository.save(result);
    }

    @Transactional
    public List<PlayerResult> createAll(List<PlayerResultDto> requests) {

        List<PlayerResult> results = requests.stream()
                .map(this::toEntity)
                .toList();

        return playerResultRepository.saveAll(results);
    }

    private PlayerResult toEntity(PlayerResultDto request) {

        PlayerResult result = new PlayerResult();

        result.setName(request.name());
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

        return result;
    }
}
