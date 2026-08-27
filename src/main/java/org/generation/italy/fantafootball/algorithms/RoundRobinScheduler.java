package org.generation.italy.fantafootball.algorithms;

import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Genera gli abbinamenti casa/trasferta con il metodo del cerchio (circle method).
// Logica pura: non conosce Matchday, League o persistenza, prende squadre e numero
// di round richiesti e restituisce solo gli accoppiamenti.
@Component
public class RoundRobinScheduler {

    public record MatchPairing(Team home, Team away) {
    }

    public List<List<MatchPairing>> generateSchedule(List<Team> teams, int totalRounds) {
        if (teams == null || teams.size() < 2) {
            throw new BadRequestException("not_enough_teams", "Servono almeno 2 squadre per generare un calendario");
        }
        if (totalRounds <= 0) {
            throw new BadRequestException("invalid_rounds", "Il numero di round da generare deve essere maggiore di zero");
        }

        List<Team> rotation = new ArrayList<>(teams);
        boolean hasBye = rotation.size() % 2 != 0;
        if (hasBye) {
            rotation.add(null);
        }

        List<List<MatchPairing>> baseCycle = buildBaseCycle(rotation);
        int cycleLength = baseCycle.size();

        List<List<MatchPairing>> schedule = new ArrayList<>();
        for (int round = 0; round < totalRounds; round++) {
            int pass = round / cycleLength;
            int positionInCycle = round % cycleLength;
            boolean flipHomeAway = pass % 2 != 0;

            List<MatchPairing> roundPairings = new ArrayList<>();
            for (MatchPairing pairing : baseCycle.get(positionInCycle)) {
                roundPairings.add(flipHomeAway
                        ? new MatchPairing(pairing.away(), pairing.home())
                        : pairing);
            }
            schedule.add(roundPairings);
        }
        return schedule;
    }

    private List<List<MatchPairing>> buildBaseCycle(List<Team> rotation) {
        int n = rotation.size();
        int cycleLength = n - 1;

        List<List<MatchPairing>> baseCycle = new ArrayList<>();
        List<Team> arr = new ArrayList<>(rotation);

        for (int round = 0; round < cycleLength; round++) {
            List<MatchPairing> roundPairings = new ArrayList<>();
            for (int i = 0; i < n / 2; i++) {
                Team home = arr.get(i);
                Team away = arr.get(n - 1 - i);
                if (home == null || away == null) {
                    continue;
                }
                roundPairings.add(round % 2 == 0
                        ? new MatchPairing(home, away)
                        : new MatchPairing(away, home));
            }
            baseCycle.add(roundPairings);

            List<Team> rotated = new ArrayList<>(n);
            rotated.add(arr.get(0));
            rotated.add(arr.get(n - 1));
            rotated.addAll(arr.subList(1, n - 1));
            arr = rotated;
        }
        return baseCycle;
    }
}
