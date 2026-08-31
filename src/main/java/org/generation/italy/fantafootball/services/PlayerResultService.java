package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.PlayerResultDto;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerResultService {

    private final PlayerResultRepository playerResultRepository;
    private final PlayerRepository playerRepository;

    public PlayerResultService(PlayerResultRepository playerResultRepository, PlayerRepository playerRepository) {
        this.playerResultRepository = playerResultRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerResult create(PlayerResultDto request) {

        PlayerResult result = toEntity(request);

        return playerResultRepository.save(result);
    }

    @Transactional
    public List<PlayerResult> createAll(List<PlayerResultDto> requests) {

        List<PlayerResult> results = requests.stream()
                .map(this::toEntity) // method reference di: .map(request -> this.toEntity(request))
                .toList();

        return playerResultRepository.saveAll(results);
    }

    private PlayerResult toEntity(PlayerResultDto request) {

        PlayerResult result = new PlayerResult();

        Player player = playerRepository.findByNameAndSurnameAndRealTeamNameAndRealTeamShirtNum(
                        request.name(), request.surname(), request.realTeamName(), request.realTeamShirtNum())
                .orElseThrow(() -> new NotFoundException("player_not_found", "Giocatore non esistente"));
        result.setPlayer(player);

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
