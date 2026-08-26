package org.generation.italy.fantafootball.calculateMatchday;

public class PlayerMatchStats {
    private Long playerId;
    private double vote;
    private int goals;
    private int goalConceded;
    private int autogoal;
    private int assists;
    private int penaltySaved;
    private int penaltyFailed;
    private int yellowCards;
    private boolean redCards;
    private boolean cleansheet;

    static double calculateFantaRatingPlayer(PlayerMatchStats player) {

        return player.vote+ 3*player.goals- player.autogoal+ player.assists+ 3*player.penaltySaved-
               3*player.penaltyFailed -0.5* player.yellowCards- (player.redCards? 1:0) + (player.cleansheet? 1:0);
    }
}
