package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;

public class PlayerMatchStats {
    private final TeamPlayer player;
    private final LineupPlayer lineupPlayer;
    private final PlayerResult result;

    public PlayerMatchStats(LineupPlayer lineupPlayer, PlayerResult result) {
        if (lineupPlayer == null || lineupPlayer.getTeamPlayer() == null) {
            throw new IllegalArgumentException("A lineup player is required");
        }
        if (result == null) {
            throw new IllegalArgumentException("A player result is required");
        }
        this.lineupPlayer = lineupPlayer;
        this.player = lineupPlayer.getTeamPlayer();
        this.result = result;
    }

    public TeamPlayer getPlayer() {
        return player;
    }

    public LineupPlayer getLineupPlayer() {
        return lineupPlayer;
    }

    public PlayerResult getResult() {
        return result;
    }

    public double getVote() {
        return result.getRating() == null ? 0.0 : result.getRating().doubleValue();
    }

    private boolean isGoalkeeper() {
        return player.getPlayer() != null
                && player.getPlayer().getRole() == PlayerRole.P;
    }

    public double calculateFantaRating() {
        return calculateFantaRating(result);
    }

    // Usa solo campi di PlayerResult (il riferimento a Player incluso), quindi calcolabile per
    // qualunque giocatore con un risultato per una giornata, senza bisogno di una lineup/formazione.
    public static double calculateFantaRating(PlayerResult result) {
        boolean isGoalkeeper = result.getPlayer() != null && result.getPlayer().getRole() == PlayerRole.P;
        double vote = result.getRating() == null ? 0.0 : result.getRating().doubleValue();
        return vote
                + 3 * result.getGoalNum()
                - result.getAutogoalNum()
                + result.getAssistNum()
                + 3 * result.getPenaltySaved()
                - 3 * result.getPenaltyFailed()
                - 0.5 * result.getYellowCard()
                - (result.isRedCard() ? 1 : 0)
                + (Boolean.TRUE.equals(result.getCleanSheet()) ? 1 : 0)
                - (isGoalkeeper ? result.getGoalConceded() : 0);
    }
}
