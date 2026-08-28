package org.generation.italy.fantafootball.calculateMatchday;

import java.util.Comparator;
import java.util.List;

public class TeamMatchStats {
    public double calculateFantaRatingLineup(MatchdayLineup lineup) {
        lineup.validate();
        double total = lineup.getPlayers().stream()
                .mapToDouble(PlayerMatchStats::calculateFantaRating)
                .sum();

        return total + calculateModBonus(lineup);
    }

    public int calculateModBonus(MatchdayLineup lineup) {
        if (!lineup.getLineup().isDefensive()) {
            return 0;
        }
        List<PlayerMatchStats> defenders = lineup.getDefenders().stream()
                .sorted(Comparator.comparing(PlayerMatchStats::getVote).reversed())
                .toList();
        PlayerMatchStats goalkeeper = lineup.getGoalkeeper();

        // Il modificatore richiede portiere e almeno quattro difensori.
        // Anche con quattro o più difensori la media usa solo i tre migliori.
        if (goalkeeper == null || defenders.size() < 4) {
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
