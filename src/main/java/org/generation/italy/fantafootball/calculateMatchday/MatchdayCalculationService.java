package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.repositories.LineupRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
        if (lineupId == null) {
            throw new IllegalArgumentException("Lineup id is required");
        }
        Lineup lineup = lineupRepository.findById(lineupId)
                .orElseThrow(() -> new NotFoundException(
                        "lineup_not_found", "Lineup not found: " + lineupId));

        if (!lineup.getLeagueMatch().getMatchday().isClosed()) {
            throw new IllegalStateException("The matchday is not closed yet");
        }

        if (!Objects.equals(lineup.getTeam().getId(), lineup.getLeagueMatch().getHomeTeam().getId())
                && !Objects.equals(lineup.getTeam().getId(), lineup.getLeagueMatch().getAwayTeam().getId())) {
            throw new IllegalArgumentException("Lineup team does not belong to the league match");
        }

        List<LineupPlayer> starters = lineup.getPlayers().stream()
                .filter(LineupPlayer::isStarter)
                .toList();
        List<LineupPlayer> substitutes = lineup.getPlayers().stream()
                .filter(player -> !player.isStarter())
                .toList();

        Set<Long> usedPlayers = new HashSet<>();
        List<PlayerMatchStats> players = new ArrayList<>();

        for (LineupPlayer starter : starters) {
            Optional<PlayerMatchStats> starterStats = toPlayedMatchStats(lineup, starter);
            if (starterStats.isPresent()) {
                players.add(starterStats.get());
                usedPlayers.add(starter.getPlayerId());
                continue;
            }

            // Le sostituzioni sono per ruolo e rispettano l'ordine della panchina
            // restituito dalla lineup. Un giocatore può entrare una sola volta.
            substitutes.stream()
                    .filter(substitute -> !usedPlayers.contains(substitute.getPlayerId()))
                    .filter(substitute -> samePosition(starter, substitute))
                    .map(substitute -> toPlayedMatchStats(lineup, substitute)
                            .map(stats -> new SubstitutionCandidate(substitute, stats)))
                    .flatMap(Optional::stream)
                    .findFirst()
                    .ifPresent(candidate -> {
                        players.add(candidate.stats());
                        usedPlayers.add(candidate.player().getPlayerId());
                    });
        }

        return teamMatchStats.calculateFantaRatingLineup(
                new MatchdayLineup(lineup, players)
        );
    }

    @Transactional(readOnly = true)
    public int calculateLineupGoals(Long lineupId) {
        return GoalsCalculator.calculateGoals(calculateLineupScore(lineupId));
    }

    private Optional<PlayerMatchStats> toPlayedMatchStats(Lineup lineup, LineupPlayer lineupPlayer) {
        var player = lineupPlayer.getTeamPlayer();
        Optional<PlayerResult> result = playerResultRepository
                .findByPlayerIdAndMatchdayId(
                        player.getPlayer().getId(),
                        lineup.getLeagueMatch().getMatchday().getId()
                );

        // Un voto nullo identifica un giocatore che non ha giocato. L'assenza
        // del record ha lo stesso effetto e non deve bloccare la giornata.
        return result.filter(playerResult -> playerResult.getRating() != null)
                .map(playerResult -> new PlayerMatchStats(lineupPlayer, playerResult));
    }

    private boolean samePosition(LineupPlayer first, LineupPlayer second) {
        return first.getPosition() != null
                && first.getPosition().equalsIgnoreCase(second.getPosition());
    }

    private record SubstitutionCandidate(LineupPlayer player, PlayerMatchStats stats) {
    }
}
