package org.generation.italy.fantafootball.calculateMatchday;

import java.util.Comparator;
import java.util.List;

import static org.generation.italy.fantafootball.calculateMatchday.Lineup.verifyModDef;
import static org.generation.italy.fantafootball.calculateMatchday.PlayerMatchStats.calculateFantaRatingPlayer;

public class TeamMatchStats {
    private Lineup lineup;
    private double teamRating;




    static int calculateModBonus(Lineup lineup) {
        verifyModDef(lineup);
        List<Player> defenders = lineup.getDefenders();

        defenders.sort(Comparator.comparing(Player::getVote).reversed());

        double sum = 0;

        for (int i = 0; i < 3; i++) {
            sum += defenders.get(i).getVote();
        }

        double avg= (sum+lineup.getGoalkeeper().getVote()) / 4;
        int bonus;

        if (avg >= 7) {
            bonus = 6;
        } else if (avg >= 6.5) {
            bonus = 3;
        } else if (avg >= 6) {
            bonus = 1;
        } else {
            bonus = 0;
        }
        return bonus;

    }

    /*static double calculateFantaRatingLineup(Lineup lineup) {

        double total = 0;

        total += calculateFantaRatingPlayer(goalKeaperStats);

        for (Player player : lineup.getDefenders()) {

            total += calculateFantaRatingPlayer(player);
        }

        for (Player player : lineup.getMidfielders()) {
            total += calculateFantaRatingPlayer(player);
        }

        for (Player player : lineup.getForwards()) {
            total += calculateFantaRatingPlayer(player);
        }

        return total;
    }*/
    static double calculateFantaRatingLineup(MatchdayLineup lineup, Lineup lineup2) {
        double total = 0;

        total += calculateFantaRatingPlayer(lineup.getGoalkeeper());

        for (PlayerMatchStats player : lineup.getDefenders()) {
            total += calculateFantaRatingPlayer(player);
        }

        for (PlayerMatchStats player : lineup.getMidfielders()) {
            total += calculateFantaRatingPlayer(player);
        }

        for (PlayerMatchStats player : lineup.getForwards()) {
            total += calculateFantaRatingPlayer(player);
        }

        total += calculateModBonus(lineup2);

        return total;
    }
}
