package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.PlayerRole;

import java.util.List;
import java.util.Set;

public class MatchdayLineup {
    private static final Set<PlayerRole> VALID_POSITIONS = Set.of(PlayerRole.values());
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
                .filter(p -> isPosition(p, PlayerRole.P))
                .findFirst()
                .orElse(null);
    }

    public void validate() {
        long goalkeepers = players.stream()
                .filter(p -> PlayerRole.P == p.getLineupPlayer().getPosition())
                .count();

        if (goalkeepers > 1) {
            throw new IllegalArgumentException("A lineup cannot have more than one goalkeeper");
        }

        boolean invalidPosition = players.stream()
                .anyMatch(p -> p.getLineupPlayer().getPosition() == null
                        || !VALID_POSITIONS.contains(p.getLineupPlayer().getPosition()));
        if (invalidPosition) {
            throw new IllegalArgumentException("Every starting player must have a valid position");
        }
    }

    public List<PlayerMatchStats> getDefenders() {
        return players.stream().filter(p -> isPosition(p, PlayerRole.D)).toList();
    }

    public List<PlayerMatchStats> getMidfielders() {
        return players.stream().filter(p -> isPosition(p, PlayerRole.C)).toList();
    }

    public List<PlayerMatchStats> getForwards() {
        return players.stream().filter(p -> isPosition(p, PlayerRole.A)).toList();
    }

    private boolean isPosition(PlayerMatchStats player, PlayerRole expected) {
        return expected == player.getLineupPlayer().getPosition();
    }
}
