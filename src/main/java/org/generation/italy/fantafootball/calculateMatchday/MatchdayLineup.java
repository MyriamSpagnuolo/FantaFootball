package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Lineup;

import java.util.List;

public class MatchdayLineup {
    private final Lineup lineup;
    private final List<PlayerMatchStats> players;

    public MatchdayLineup(Lineup lineup, List<PlayerMatchStats> players) {
        this.lineup = lineup;
        this.players = List.copyOf(players);
    }

    public Lineup getLineup() {
        return lineup;
    }

    public List<PlayerMatchStats> getPlayers() {
        return players;
    }

    public PlayerMatchStats getGoalkeeper() {
        return players.stream()
                .filter(p -> isPosition(p, "P"))
                .filter(p -> p.getLineupPlayer().isStarter())
                .findFirst()
                .orElse(null);
    }

    public List<PlayerMatchStats> getDefenders() {
        return players.stream().filter(p -> isPosition(p, "D")).toList();
    }

    public List<PlayerMatchStats> getMidfielders() {
        return players.stream().filter(p -> isPosition(p, "C")).toList();
    }

    public List<PlayerMatchStats> getForwards() {
        return players.stream().filter(p -> isPosition(p, "A")).toList();
    }

    private boolean isPosition(PlayerMatchStats player, String expected) {
        return expected.equalsIgnoreCase(player.getLineupPlayer().getPosition());
    }
}
