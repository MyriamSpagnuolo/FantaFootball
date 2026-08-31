package org.generation.italy.fantafootball.algorithms;

import org.generation.italy.fantafootball.algorithms.RoundRobinScheduler.MatchPairing;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoundRobinSchedulerTest {

    private final RoundRobinScheduler scheduler = new RoundRobinScheduler();

    private static List<Team> teams(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    Team team = new Team();
                    team.setName("Team " + i);
                    return team;
                })
                .collect(Collectors.toList());
    }

    private static String pairKey(Team a, Team b) {
        return Stream.of(a.getName(), b.getName()).sorted().collect(Collectors.joining("-"));
    }

    @Nested
    @DisplayName("Validazioni")
    class Validations {

        @Test
        @DisplayName("rifiuta una lista di squadre null")
        void rejectsNullTeams() {
            assertThatThrownBy(() -> scheduler.generateSchedule(null, 3))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("rifiuta meno di due squadre")
        void rejectsFewerThanTwoTeams() {
            assertThatThrownBy(() -> scheduler.generateSchedule(teams(1), 3))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("rifiuta un numero di round non positivo")
        void rejectsNonPositiveRounds() {
            List<Team> teams = teams(4);
            assertThatThrownBy(() -> scheduler.generateSchedule(teams, 0))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("Numero di squadre pari")
    class EvenTeams {

        @Test
        @DisplayName("un ciclo completo genera N-1 round da N/2 partite ciascuno")
        void fullCycleHasExpectedShape() {
            List<Team> teams = teams(4);

            List<List<MatchPairing>> schedule = scheduler.generateSchedule(teams, 3);

            assertThat(schedule).hasSize(3);
            schedule.forEach(round -> assertThat(round).hasSize(2));
        }

        @Test
        @DisplayName("in un ciclo completo ogni coppia di squadre si incontra esattamente una volta")
        void fullCycleCoversEveryPairOnce() {
            List<Team> teams = teams(4);

            List<List<MatchPairing>> schedule = scheduler.generateSchedule(teams, 3);

            Set<String> pairsPlayed = schedule.stream()
                    .flatMap(List::stream)
                    .map(p -> pairKey(p.home(), p.away()))
                    .collect(Collectors.toSet());

            assertThat(pairsPlayed).hasSize(6); // C(4,2)
        }

        @Test
        @DisplayName("nessuna squadra gioca due volte nello stesso round")
        void noTeamPlaysTwiceInTheSameRound() {
            List<Team> teams = teams(6);

            List<List<MatchPairing>> schedule = scheduler.generateSchedule(teams, 5);

            for (List<MatchPairing> round : schedule) {
                List<Team> teamsInRound = new ArrayList<>();
                round.forEach(p -> {
                    teamsInRound.add(p.home());
                    teamsInRound.add(p.away());
                });
                assertThat(teamsInRound).doesNotHaveDuplicates();
            }
        }
    }

    @Nested
    @DisplayName("Numero di squadre dispari (bye)")
    class OddTeams {

        @Test
        @DisplayName("con N dispari ogni round ha una squadra a riposo")
        void oneTeamRestsPerRound() {
            List<Team> teams = teams(3);

            List<List<MatchPairing>> schedule = scheduler.generateSchedule(teams, 3);

            schedule.forEach(round -> assertThat(round).hasSize(1));
        }

        @Test
        @DisplayName("con N dispari ogni coppia si incontra comunque esattamente una volta nel ciclo")
        void byeStillCoversEveryPairOnce() {
            List<Team> teams = teams(5);

            // ciclo completo per 5 squadre (+bye) = 5 round
            List<List<MatchPairing>> schedule = scheduler.generateSchedule(teams, 5);

            Set<String> pairsPlayed = schedule.stream()
                    .flatMap(List::stream)
                    .map(p -> pairKey(p.home(), p.away()))
                    .collect(Collectors.toSet());

            assertThat(pairsPlayed).hasSize(10); // C(5,2)
        }
    }

    @Nested
    @DisplayName("Round oltre il ciclo base")
    class RepeatedCycles {

        @Test
        @DisplayName("richiedendo più round del ciclo base, il ciclo si ripete invertendo casa/trasferta")
        void repeatsCycleFlippingHomeAway() {
            List<Team> teams = teams(4); // ciclo base = 3 round
            int cycleLength = 3;

            List<List<MatchPairing>> schedule = scheduler.generateSchedule(teams, cycleLength * 2);

            assertThat(schedule).hasSize(cycleLength * 2);

            List<MatchPairing> firstCycleRound = schedule.get(0);
            List<MatchPairing> secondCycleRound = schedule.get(cycleLength);

            assertThat(secondCycleRound).hasSameSizeAs(firstCycleRound);
            for (int i = 0; i < firstCycleRound.size(); i++) {
                MatchPairing original = firstCycleRound.get(i);
                MatchPairing repeated = secondCycleRound.get(i);
                assertThat(repeated.home()).isEqualTo(original.away());
                assertThat(repeated.away()).isEqualTo(original.home());
            }
        }

        @Test
        @DisplayName("il numero di round generati corrisponde esattamente a quello richiesto, anche oltre il ciclo base")
        void generatesExactlyTheRequestedNumberOfRounds() {
            List<Team> teams = teams(4);

            List<List<MatchPairing>> schedule = scheduler.generateSchedule(teams, 7);

            assertThat(schedule).hasSize(7);
        }
    }
}
