package org.generation.italy.fantafootball.calculateMatchday;

import org.generation.italy.fantafootball.model.dto.TeamPlayerRatingResponse;
import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.repositories.LeagueMatchRepository;
import org.generation.italy.fantafootball.model.repositories.LineupRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class MatchdayCalculationService {

    private final LineupRepository lineupRepository;
    private final PlayerResultRepository playerResultRepository;
    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LeagueMatchRepository leagueMatchRepository;
    private final TeamMatchStats teamMatchStats = new TeamMatchStats();

    public MatchdayCalculationService(
            LineupRepository lineupRepository,
            PlayerResultRepository playerResultRepository,
            TeamRepository teamRepository,
            TeamPlayerRepository teamPlayerRepository,
            LeagueMatchRepository leagueMatchRepository
    ) {
        this.lineupRepository = lineupRepository;
        this.playerResultRepository = playerResultRepository;
        this.teamRepository = teamRepository;
        this.teamPlayerRepository = teamPlayerRepository;
        this.leagueMatchRepository = leagueMatchRepository;
    }

    @Transactional(readOnly = true)
    public double calculateLineupScore(Long lineupId) {
        if (lineupId == null) {
            throw new BadRequestException("lineup_id_required", "Lineup id is required");
        }
        Lineup lineup = lineupRepository.findById(lineupId)
                .orElseThrow(() -> new NotFoundException(
                        "lineup_not_found", "Lineup not found: " + lineupId));

        if (!lineup.getLeagueMatch().getMatchday().isClosed()) {
            throw new ConflictException("matchday_not_closed", "The matchday is not closed yet");
        }

        if (!Objects.equals(lineup.getTeam().getId(), lineup.getLeagueMatch().getHomeTeam().getId())
                && !Objects.equals(lineup.getTeam().getId(), lineup.getLeagueMatch().getAwayTeam().getId())) {
            throw new ConflictException("lineup_team_mismatch", "Lineup team does not belong to the league match");
        }

        List<LineupPlayer> starters = lineup.getPlayers().stream()
                .filter(LineupPlayer::isStarter)
                .toList();
        List<LineupPlayer> substitutes = lineup.getPlayers().stream()
                .filter(player -> !player.isStarter())
                .toList();

        Set<Long> usedPlayers = new HashSet<>();
        List<PlayerMatchStats> players = new ArrayList<>();

        for (LineupPlayer starter : starters) {
            Optional<PlayerMatchStats> starterStats = toPlayedMatchStats(lineup, starter);
            if (starterStats.isPresent()) {
                players.add(starterStats.get());
                usedPlayers.add(starter.getPlayerId());
                continue;
            }

            // Le sostituzioni sono per ruolo e rispettano l'ordine della panchina
            // restituito dalla lineup. Un giocatore deve entrare una sola volta.
            substitutes.stream()
                    .filter(substitute -> !usedPlayers.contains(substitute.getPlayerId()))
                    .filter(substitute -> sameRole(starter, substitute))
                    .map(substitute -> toPlayedMatchStats(lineup, substitute)
                            .map(stats -> new SubstitutionCandidate(substitute, stats)))
                    .flatMap(Optional::stream)
                    .findFirst()
                    .ifPresent(candidate -> {
                        players.add(candidate.stats());
                        usedPlayers.add(candidate.player().getPlayerId());
                    });
        }

        return teamMatchStats.calculateFantaRatingLineup(
                new MatchdayLineup(lineup, players)
        );
    }

    @Transactional(readOnly = true)
    public int calculateLineupGoals(Long lineupId) {
        return GoalsCalculator.calculateGoals(calculateLineupScore(lineupId));
    }

    // Fantavoto del singolo giocatore per una giornata, indipendente da qualunque lineup: utile
    // per chi non e' mai stato schierato da nessuna fantasquadra (o e' stato messo in panchina e
    // mai sostituito), casi che calculateLineupScore non copre perche' itera solo lineup.getPlayers().
    @Transactional(readOnly = true)
    public double calculatePlayerRating(Long playerId, Long matchdayId) {
        PlayerResult result = playerResultRepository.findByPlayerIdAndMatchdayId(playerId, matchdayId)
                .orElseThrow(() -> new NotFoundException(
                        "player_result_not_found",
                        "No result found for player " + playerId + " in matchday " + matchdayId));
        return PlayerMatchStats.calculateFantaRating(result);
    }

    // Fantavoto di TUTTA la rosa attiva di una squadra per la giornata di una sua partita di lega,
    // non solo dei giocatori schierati in quella lineup: serve a confrontare chi ha reso meglio
    // (anche chi e' rimasto in panchina o non e' mai stato messo in formazione), per decidere chi
    // schierare alla prossima giornata. leagueMatchId (non matchdayId) perche' e' gia' l'identificativo
    // che il frontend conosce dal calendario/dalla lineup — la matchday non e' esposta altrove.
    // Un giocatore senza ancora un PlayerResult per quella giornata (non ha giocato, o la giornata
    // non e' chiusa) compare comunque nella risposta con fantaRating = null, non viene escluso ne'
    // fa fallire l'intera chiamata.
    @Transactional(readOnly = true)
    public List<TeamPlayerRatingResponse> calculateTeamRosterRatings(Long teamId, Long leagueMatchId, Long requestingUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("TEAM_NOT_FOUND", "Squadra non trovata: " + teamId));

        boolean isOwner = team.getUser().getId().equals(requestingUserId);
        boolean isLeagueAdmin = team.getLeague().getAdmin().getId().equals(requestingUserId);
        if (!isOwner && !isLeagueAdmin) {
            throw new AccessDeniedException("Solo il proprietario della squadra o l'admin della lega possono vedere questi voti");
        }

        LeagueMatch leagueMatch = leagueMatchRepository.findById(leagueMatchId)
                .orElseThrow(() -> new NotFoundException("league_match_not_found", "Partita non trovata: " + leagueMatchId));

        boolean teamPlaysMatch = Objects.equals(leagueMatch.getHomeTeam().getId(), teamId)
                || Objects.equals(leagueMatch.getAwayTeam().getId(), teamId);
        if (!teamPlaysMatch) {
            throw new ConflictException("lineup_team_mismatch", "La squadra non gioca questa partita di lega");
        }

        Long matchdayId = leagueMatch.getMatchday().getId();
        List<TeamPlayer> roster = teamPlayerRepository.findAllByTeamIdAndTransferDateIsNull(teamId);

        return roster.stream()
                .map(teamPlayer -> {
                    Double fantaRating = playerResultRepository
                            .findByPlayerIdAndMatchdayId(teamPlayer.getPlayer().getId(), matchdayId)
                            .map(PlayerMatchStats::calculateFantaRating)
                            .orElse(null);
                    return TeamPlayerRatingResponse.of(teamPlayer, fantaRating);
                })
                .toList();
    }

    private Optional<PlayerMatchStats> toPlayedMatchStats(Lineup lineup, LineupPlayer lineupPlayer) {
        var player = lineupPlayer.getTeamPlayer();
        Optional<PlayerResult> result = playerResultRepository
                .findByPlayerIdAndMatchdayId(
                        player.getPlayer().getId(),
                        lineup.getLeagueMatch().getMatchday().getId()
                );

        // Un voto nullo identifica un giocatore che non ha giocato. L'assenza
        // del record ha lo stesso effetto e non deve bloccare la giornata.
        return result.filter(playerResult -> playerResult.getRating() != null)
                .map(playerResult -> new PlayerMatchStats(lineupPlayer, playerResult));
    }

    private boolean sameRole(LineupPlayer first, LineupPlayer second) {
        return first.getRole() != null
                && first.getRole() == second.getRole();
    }

    private record SubstitutionCandidate(LineupPlayer player, PlayerMatchStats stats) {
    }
}
