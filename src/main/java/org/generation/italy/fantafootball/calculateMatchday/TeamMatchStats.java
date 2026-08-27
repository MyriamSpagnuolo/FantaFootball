package org.generation.italy.fantafootball.calculateMatchday;

import java.util.Comparator;
import java.util.List;

import static org.generation.italy.fantafootball.calculateMatchday.Lineup.verifyModDef;

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

        double avg= sum+lineup.getGoalkeeper().getVote() / 4;
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

    public double calculateTeamRating(Lineup lineup){

        double a =0;
     return  a;
    }

}
