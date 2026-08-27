package org.generation.italy.fantafootball.calculateMatchday;

import java.util.List;

public class MatchdayLineup {
    private PlayerMatchStats goalkeeper;
    private List<PlayerMatchStats> defenders;
    private List<PlayerMatchStats> midfielders;
    private List<PlayerMatchStats> forwards;

    public PlayerMatchStats getGoalkeeper() {
        return goalkeeper;
    }

    public List<PlayerMatchStats> getDefenders() {
        return defenders;
    }

    public List<PlayerMatchStats> getMidfielders() {
        return midfielders;
    }

    public List<PlayerMatchStats> getForwards() {
        return forwards;
    }
}
