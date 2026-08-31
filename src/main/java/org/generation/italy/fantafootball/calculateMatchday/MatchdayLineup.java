package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Lineup;

import java.util.List;
import java.util.Set;

public class MatchdayLineup {
    private static final Set<String> VALID_POSITIONS = Set.of("P", "D", "C", "A");
    private final Lineup lineup;
    private final List<PlayerMatchStats> players;

    public MatchdayLineup(Lineup lineup, List<PlayerMatchStats> players) {
        if (lineup == null) {
            throw new IllegalArgumentException("A lineup is required");
        }
        if (players == null) {
            throw new IllegalArgumentException("Players are required");
        }
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
                .findFirst()
                .orElse(null);
    }

    public void validate() {
        long goalkeepers = players.stream()
                .filter(p -> "P".equalsIgnoreCase(p.getLineupPlayer().getPosition()))
                .count();

        if (goalkeepers > 1) {
            throw new IllegalArgumentException("A lineup cannot have more than one goalkeeper");
        }

        boolean invalidPosition = players.stream()
                .anyMatch(p -> p.getLineupPlayer().getPosition() == null
                        || !VALID_POSITIONS.contains(p.getLineupPlayer().getPosition().toUpperCase()));
        if (invalidPosition) {
            throw new IllegalArgumentException("Every starting player must have a valid position");
        }
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
