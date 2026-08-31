package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Team;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aggiorna la classifica della lega a partire dal risultato di una partita.
 *
 * <p>I gol ricevuti qui sono quelli calcolati da {@link GoalsCalculator} a
 * partire dal punteggio delle lineup.</p>
 */
@Service
public class Standing {

    public static final int WIN_POINTS = 3;
    public static final int DRAW_POINTS = 1;
    public static final int LOSS_POINTS = 0;

    /** Una riga della classifica, comprensiva del criterio di spareggio. */
    public record StandingEntry(Team team, int points, double fantasyPoints) {
    }

    /**
     * Restituisce i punti assegnati a una squadra in base ai gol segnati.
     */
    public int calculatePoints(int goalsFor, int goalsAgainst) {
        validateGoals(goalsFor, goalsAgainst);
        if (goalsFor > goalsAgainst) {
            return WIN_POINTS;
        }
        if (goalsFor < goalsAgainst) {
            return LOSS_POINTS;
        }
        return DRAW_POINTS;
    }

    /**
     * Mantiene anche il nome usato nella richiesta originale.
     * @deprecated usare {@link #calculatePoints(int, int)}.
     */
    @Deprecated
    public int claculatePoints(int goalsFor, int goalsAgainst) {
        return calculatePoints(goalsFor, goalsAgainst);
    }

    /**
     * Aggiorna i punti totali delle due squadre dopo una partita.
     */
    public void updateStanding(
            Team homeTeam,
            Team awayTeam,
            int homeGoals,
            int awayGoals
    ) {
        if (homeTeam == null || awayTeam == null) {
            throw new IllegalArgumentException("Both teams are required");
        }
        if (homeTeam == awayTeam) {
            throw new IllegalArgumentException("A team cannot play against itself");
        }

        homeTeam.setTotalPoints(homeTeam.getTotalPoints() + calculatePoints(homeGoals, awayGoals));
        awayTeam.setTotalPoints(awayTeam.getTotalPoints() + calculatePoints(awayGoals, homeGoals));
    }

    /**
     * Aggiorna la classifica usando il risultato già calcolato della partita.
     */
    public void updateStanding(LeagueMatch match) {
        if (match == null || match.getHomeTeam() == null || match.getAwayTeam() == null) {
            throw new IllegalArgumentException("A valid league match is required");
        }
        if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
            throw new IllegalStateException("The match goals have not been calculated yet");
        }
        applyMatchPoints(match,
                match.getHomeGoals(), match.getAwayGoals(),
                match.getHomeGoals(), match.getAwayGoals());
    }

    /**
     * Converte i punteggi delle lineup in gol, salva il risultato sulla partita
     * e aggiorna la classifica.
     */
    public void updateStanding(LeagueMatch match, double homeScore, double awayScore) {
        if (match == null || match.getHomeTeam() == null || match.getAwayTeam() == null) {
            throw new IllegalArgumentException("A league match is required");
        }

        Integer previousHomeGoals = match.getHomeGoals();
        Integer previousAwayGoals = match.getAwayGoals();
        if ((previousHomeGoals == null) != (previousAwayGoals == null)) {
            throw new IllegalStateException("The match has an incomplete result");
        }

        int homeGoals = GoalsCalculator.calculateGoals(homeScore);
        int awayGoals = GoalsCalculator.calculateGoals(awayScore);

        match.setHomeScore(BigDecimal.valueOf(homeScore));
        match.setAwayScore(BigDecimal.valueOf(awayScore));
        match.setHomeGoals(homeGoals);
        match.setAwayGoals(awayGoals);
        applyMatchPoints(match, homeGoals, awayGoals, previousHomeGoals, previousAwayGoals);
    }

    /**
     * Calcola i fantapunti complessivi di una squadra sulle partite già
     * calcolate. Vengono considerati solo i punteggi presenti in
     * {@code home_score} e {@code away_score}; una partita non ancora
     * calcolata non contribuisce allo spareggio.
     */
    public double calculateFantasyPoints(Team team, List<LeagueMatch> matches) {
        if (team == null || matches == null) {
            throw new IllegalArgumentException("Team and matches are required");
        }

        return matches.stream()
                .filter(Objects::nonNull)
                .filter(match -> belongsTo(match, team))
                .filter(match -> match.getHomeScore() != null && match.getAwayScore() != null)
                .mapToDouble(match -> scoreFor(match, team))
                .sum();
    }

    /**
     * Restituisce la classifica ordinata per punti decrescenti e, a parità di
     * punti, per fantapunti decrescenti.
     */
    public List<StandingEntry> calculateStanding(List<Team> teams, List<LeagueMatch> matches) {
        if (teams == null || matches == null) {
            throw new IllegalArgumentException("Teams and matches are required");
        }

        Map<Team, Double> fantasyPoints = new HashMap<>();
        for (Team team : teams) {
            if (team == null) {
                throw new IllegalArgumentException("Teams cannot contain null values");
            }
            fantasyPoints.put(team, calculateFantasyPoints(team, matches));
        }

        return teams.stream()
                .map(team -> new StandingEntry(team, team.getTotalPoints(), fantasyPoints.get(team)))
                .sorted(Comparator
                        .comparingInt(StandingEntry::points).reversed()
                        .thenComparing(Comparator.comparingDouble(StandingEntry::fantasyPoints).reversed())
                        .thenComparing(entry -> entry.team().getName(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    /**
     * Stampa la classifica completa con il criterio di spareggio applicato.
     */
    public void printStanding(List<Team> teams, List<LeagueMatch> matches) {
        List<StandingEntry> standing = calculateStanding(teams, matches);
        System.out.println("CLASSIFICA");
        for (int i = 0; i < standing.size(); i++) {
            StandingEntry entry = standing.get(i);
            System.out.printf("%d. %s - %d punti (%.2f fantapunti)%n",
                    i + 1,
                    entry.team().getName(),
                    entry.points(),
                    entry.fantasyPoints());
        }
    }

    private boolean belongsTo(LeagueMatch match, Team team) {
        return sameTeam(match.getHomeTeam(), team) || sameTeam(match.getAwayTeam(), team);
    }

    private double scoreFor(LeagueMatch match, Team team) {
        if (sameTeam(match.getHomeTeam(), team)) {
            return match.getHomeScore() == null ? 0.0 : match.getHomeScore().doubleValue();
        }
        return match.getAwayScore() == null ? 0.0 : match.getAwayScore().doubleValue();
    }

    private boolean sameTeam(Team first, Team second) {
        if (first == null || second == null) {
            return false;
        }
        return first == second
                || (first.getId() != null && Objects.equals(first.getId(), second.getId()));
    }

    private void applyMatchPoints(
            LeagueMatch match,
            int homeGoals,
            int awayGoals,
            Integer previousHomeGoals,
            Integer previousAwayGoals
    ) {
        int oldHomePoints = previousHomeGoals == null
                ? 0
                : calculatePoints(previousHomeGoals, previousAwayGoals);
        int oldAwayPoints = previousHomeGoals == null
                ? 0
                : calculatePoints(previousAwayGoals, previousHomeGoals);

        int newHomePoints = calculatePoints(homeGoals, awayGoals);
        int newAwayPoints = calculatePoints(awayGoals, homeGoals);

        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();
        homeTeam.setTotalPoints(homeTeam.getTotalPoints() - oldHomePoints + newHomePoints);
        awayTeam.setTotalPoints(awayTeam.getTotalPoints() - oldAwayPoints + newAwayPoints);
    }

    private void validateGoals(int goalsFor, int goalsAgainst) {
        if (goalsFor < 0 || goalsAgainst < 0) {
            throw new IllegalArgumentException("Goals cannot be negative");
        }
    }
}
