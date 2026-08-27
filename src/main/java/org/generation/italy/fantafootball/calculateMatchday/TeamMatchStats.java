package org.generation.italy.fantafootball.calculateMatchday;

import java.util.Comparator;
import java.util.List;

public class TeamMatchStats {
    public double calculateFantaRatingLineup(MatchdayLineup lineup) {
        double total = lineup.getPlayers().stream()
                .filter(player -> player.getLineupPlayer().isStarter())
                .mapToDouble(PlayerMatchStats::calculateFantaRating)
                .sum();

        return total + calculateModBonus(lineup);
    }

    public int calculateModBonus(MatchdayLineup lineup) {
        List<PlayerMatchStats> defenders = lineup.getDefenders().stream()
                .filter(player -> player.getLineupPlayer().isStarter())
                .sorted(Comparator.comparing(PlayerMatchStats::getVote).reversed())
                .toList();
        PlayerMatchStats goalkeeper = lineup.getGoalkeeper();

        // Il modificatore esistente richiede portiere e almeno tre difensori.
        if (goalkeeper == null || defenders.size() < 3) {
            return 0;
        }

        double sum = defenders.stream().limit(3).mapToDouble(PlayerMatchStats::getVote).sum();
        double average = (sum + goalkeeper.getVote()) / 4;

        if (average >= 7) return 6;
        if (average >= 6.5) return 3;
        if (average >= 6) return 1;
        return 0;
    }
}
