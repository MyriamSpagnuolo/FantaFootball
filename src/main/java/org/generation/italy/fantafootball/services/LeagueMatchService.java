package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.algorithms.RoundRobinScheduler;
import org.generation.italy.fantafootball.algorithms.RoundRobinScheduler.MatchPairing;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LeagueMatchRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.MatchdayRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeagueMatchService {
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final MatchdayRepository matchdayRepository;
    private final LeagueMatchRepository leagueMatchRepository;
    private final RoundRobinScheduler roundRobinScheduler;

    public LeagueMatchService(
            LeagueRepository leagueRepository,
            TeamRepository teamRepository,
            MatchdayRepository matchdayRepository,
            LeagueMatchRepository leagueMatchRepository,
            RoundRobinScheduler roundRobinScheduler
    ) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.matchdayRepository = matchdayRepository;
        this.leagueMatchRepository = leagueMatchRepository;
        this.roundRobinScheduler = roundRobinScheduler;
    }

    // Genera il calendario di una lega: una giornata di lega (round_number) per ogni
    // matchday reale ancora aperta, ripetendo il ciclo round-robin finche' non le copre tutte.
    @Transactional
    public List<LeagueMatch> generateCalendar(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("league_not_found", "Lega non trovata: " + leagueId));

        if (leagueMatchRepository.existsByLeagueId(leagueId)) {
            throw new ConflictException("calendar_already_generated", "Il calendario per questa lega è già stato generato");
        }

        List<Team> teams = teamRepository.findAllTeamByLeagueId(leagueId);
        List<Matchday> availableMatchdays = matchdayRepository.findByClosedFalseOrderByDateAsc();

        List<List<MatchPairing>> schedule = roundRobinScheduler.generateSchedule(teams, availableMatchdays.size());

        List<LeagueMatch> matches = new ArrayList<>();

        for (int round = 0; round < schedule.size(); round++) {
            Matchday matchday = availableMatchdays.get(round);
            LocalDateTime matchDay = matchday.getDate().atStartOfDay();
            int roundNumber = round + 1;

            for (MatchPairing pairing : schedule.get(round)) {
                matches.add(new LeagueMatch(league, matchDay, pairing.home(), pairing.away(), matchday, roundNumber));
            }
        }

        return leagueMatchRepository.saveAll(matches);
    }
}
