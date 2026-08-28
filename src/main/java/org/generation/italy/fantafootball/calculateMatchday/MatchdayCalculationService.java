package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.repositories.LineupRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchdayCalculationService {

    private final LineupRepository lineupRepository;
    private final PlayerResultRepository playerResultRepository;
    private final TeamMatchStats teamMatchStats = new TeamMatchStats();

    public MatchdayCalculationService(
            LineupRepository lineupRepository,
            PlayerResultRepository playerResultRepository
    ) {
        this.lineupRepository = lineupRepository;
        this.playerResultRepository = playerResultRepository;
    }

    @Transactional(readOnly = true)
    public double calculateLineupScore(Long lineupId) {
        Lineup lineup = lineupRepository.findById(lineupId)
                .orElseThrow(() -> new NotFoundException(
                        "lineup_not_found", "Lineup not found: " + lineupId));

        List<PlayerMatchStats> players = lineup.getPlayers().stream()
                .filter(LineupPlayer::isStarter)
                .map(player -> toMatchStats(lineup, player))
                .toList();

        return teamMatchStats.calculateFantaRatingLineup(
                new MatchdayLineup(lineup, players)
        );
    }

    @Transactional(readOnly = true)
    public int calculateLineupGoals(Long lineupId) {
        return GoalsCalculator.calculateGoals(calculateLineupScore(lineupId));
    }

    private PlayerMatchStats toMatchStats(Lineup lineup, LineupPlayer lineupPlayer) {
        var player = lineupPlayer.getTeamPlayer();
        PlayerResult result = playerResultRepository
                .findByPlayerIdAndMatchdayId(
                        player.getPlayer().getId(),
                        lineup.getLeagueMatch().getMatchday().getId()
                )
                .orElseThrow(() -> new NotFoundException(
                        "player_result_not_found",
                        "Result not found for player: " + player.getPlayer().getName() + " " + player.getPlayer().getSurname()
                ));

        return new PlayerMatchStats(lineupPlayer, result);
    }
}
