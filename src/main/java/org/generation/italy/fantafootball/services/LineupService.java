package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.LineupPlayerRequest;
import org.generation.italy.fantafootball.model.dto.LineupPlayerResponse;
import org.generation.italy.fantafootball.model.dto.LineupRequest;
import org.generation.italy.fantafootball.model.dto.LineupResponse;
import org.generation.italy.fantafootball.model.entities.LeagueMatch;
import org.generation.italy.fantafootball.model.entities.Lineup;
import org.generation.italy.fantafootball.model.entities.LineupPlayer;
import org.generation.italy.fantafootball.model.entities.LineupType;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.LeagueMatchRepository;
import org.generation.italy.fantafootball.model.repositories.LineupPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.LineupRepository;
import org.generation.italy.fantafootball.model.repositories.LineupTypeRepository;
import org.generation.italy.fantafootball.model.repositories.TeamPlayerRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class LineupService {

    private final LineupRepository lineupRepository;
    private final LineupPlayerRepository lineupPlayerRepository;
    private final LineupTypeRepository lineupTypeRepository;
    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LeagueMatchRepository leagueMatchRepository;

    public LineupService(
            LineupRepository lineupRepository,
            LineupPlayerRepository lineupPlayerRepository,
            LineupTypeRepository lineupTypeRepository,
            TeamRepository teamRepository,
            TeamPlayerRepository teamPlayerRepository,
            LeagueMatchRepository leagueMatchRepository
    ) {
        this.lineupRepository = lineupRepository;
        this.lineupPlayerRepository = lineupPlayerRepository;
        this.lineupTypeRepository = lineupTypeRepository;
        this.teamRepository = teamRepository;
        this.teamPlayerRepository = teamPlayerRepository;
        this.leagueMatchRepository = leagueMatchRepository;
    }

    @Transactional
    public LineupResponse createLineup(Long teamId, Long leagueMatchId, LineupRequest request, Long requestingUserId) {
        Team team = loadOwnedTeam(teamId, requestingUserId);
        LeagueMatch leagueMatch = loadMatchForTeam(leagueMatchId, team);
        ensureMatchdayOpen(leagueMatch);

        if (lineupRepository.existsByTeamIdAndLeagueMatchId(teamId, leagueMatchId)) {
            throw new ConflictException("lineup_already_exists",
                    "Esiste già una formazione per questa partita: usa l'update per modificarla");
        }

        LineupType lineupType = loadLineupType(request.lineupTypeId());
        Map<Long, TeamPlayer> teamPlayersById = validateAndLoadPlayers(team, lineupType, request.players());

        Lineup lineup = new Lineup(team, leagueMatch, lineupType, request.defensive());
        Lineup savedLineup = lineupRepository.save(lineup);

        List<LineupPlayerResponse> playersResponse = savePlayers(savedLineup, request.players(), teamPlayersById);
        return LineupResponse.fromEntity(savedLineup, playersResponse);
    }

    @Transactional(readOnly = true)
    public LineupResponse getLineup(Long teamId, Long leagueMatchId, Long requestingUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("TEAM_NOT_FOUND", "Squadra non trovata: " + teamId));

        boolean isOwner = team.getUser().getId().equals(requestingUserId);
        boolean isLeagueAdmin = team.getLeague().getAdmin().getId().equals(requestingUserId);
        if (!isOwner && !isLeagueAdmin) {
            throw new AccessDeniedException("Solo il proprietario della squadra o l'admin della lega possono vedere questa formazione");
        }

        Lineup lineup = lineupRepository.findByTeamIdAndLeagueMatchId(teamId, leagueMatchId)
                .orElseThrow(() -> new NotFoundException("lineup_not_found", "Nessuna formazione trovata per questa partita"));

        List<LineupPlayerResponse> players = lineup.getPlayers().stream()
                .map(LineupPlayerResponse::fromEntity)
                .toList();
        return LineupResponse.fromEntity(lineup, players);
    }

    @Transactional
    public LineupResponse updateLineup(Long teamId, Long leagueMatchId, LineupRequest request, Long requestingUserId) {
        Team team = loadOwnedTeam(teamId, requestingUserId);
        LeagueMatch leagueMatch = loadMatchForTeam(leagueMatchId, team);
        ensureMatchdayOpen(leagueMatch);

        Lineup lineup = lineupRepository.findByTeamIdAndLeagueMatchId(teamId, leagueMatchId)
                .orElseThrow(() -> new NotFoundException("lineup_not_found",
                        "Nessuna formazione da modificare per questa partita: creala prima"));

        LineupType lineupType = loadLineupType(request.lineupTypeId());
        Map<Long, TeamPlayer> teamPlayersById = validateAndLoadPlayers(team, lineupType, request.players());

        lineup.setLineupType(lineupType);
        lineup.setDefensive(request.defensive());
        lineupRepository.save(lineup);

        lineupPlayerRepository.deleteAllByLineup_Id(lineup.getId());
        List<LineupPlayerResponse> playersResponse = savePlayers(lineup, request.players(), teamPlayersById);
        return LineupResponse.fromEntity(lineup, playersResponse);
    }

    private Team loadOwnedTeam(Long teamId, Long requestingUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("TEAM_NOT_FOUND", "Squadra non trovata: " + teamId));

        if (!team.getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("Solo il proprietario può gestire la formazione della squadra");
        }
        return team;
    }

    private LeagueMatch loadMatchForTeam(Long leagueMatchId, Team team) {
        LeagueMatch leagueMatch = leagueMatchRepository.findById(leagueMatchId)
                .orElseThrow(() -> new NotFoundException("league_match_not_found", "Partita non trovata: " + leagueMatchId));

        boolean teamPlaysMatch = Objects.equals(leagueMatch.getHomeTeam().getId(), team.getId())
                || Objects.equals(leagueMatch.getAwayTeam().getId(), team.getId());
        if (!teamPlaysMatch) {
            throw new ConflictException("lineup_team_mismatch", "La squadra non gioca questa partita di lega");
        }
        return leagueMatch;
    }

    private void ensureMatchdayOpen(LeagueMatch leagueMatch) {
        if (leagueMatch.getMatchday().isClosed()) {
            throw new ConflictException("matchday_closed", "La giornata è già chiusa: la formazione non è più modificabile");
        }
    }

    private LineupType loadLineupType(Long lineupTypeId) {
        return lineupTypeRepository.findById(lineupTypeId)
                .orElseThrow(() -> new NotFoundException("lineup_type_not_found", "Modulo non trovato: " + lineupTypeId));
    }

    private Map<Long, TeamPlayer> validateAndLoadPlayers(Team team, LineupType lineupType, List<LineupPlayerRequest> players) {
        List<Long> requestedIds = players.stream().map(LineupPlayerRequest::teamPlayerId).toList();
        long distinctCount = requestedIds.stream().distinct().count();
        if (distinctCount != requestedIds.size()) {
            throw new BadRequestException("duplicate_players", "Lo stesso giocatore non può comparire più volte in formazione");
        }

        List<TeamPlayer> activeTeamPlayers = teamPlayerRepository
                .findAllByIdInAndTeamIdAndTransferDateIsNull(requestedIds, team.getId());
        if (activeTeamPlayers.size() != requestedIds.size()) {
            throw new BadRequestException("invalid_team_players",
                    "Uno o più giocatori non appartengono alla rosa attiva di questa squadra");
        }

        Map<Long, TeamPlayer> teamPlayersById = activeTeamPlayers.stream()
                .collect(java.util.stream.Collectors.toMap(TeamPlayer::getId, Function.identity()));

        validateFormation(lineupType, players, teamPlayersById);

        return teamPlayersById;
    }

    private void validateFormation(LineupType lineupType, List<LineupPlayerRequest> players, Map<Long, TeamPlayer> teamPlayersById) {
        List<LineupPlayerRequest> starters = players.stream().filter(LineupPlayerRequest::starter).toList();

        Map<PlayerRole, Long> starterCountByRole = starters.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> teamPlayersById.get(p.teamPlayerId()).getPlayer().getRole(),
                        java.util.stream.Collectors.counting()));

        long goalkeepers = starterCountByRole.getOrDefault(PlayerRole.P, 0L);
        long defenders = starterCountByRole.getOrDefault(PlayerRole.D, 0L);
        long midfielders = starterCountByRole.getOrDefault(PlayerRole.C, 0L);
        long forwards = starterCountByRole.getOrDefault(PlayerRole.A, 0L);

        boolean matchesFormation = goalkeepers == 1
                && defenders == lineupType.getDefenderNum()
                && midfielders == lineupType.getMidfielderNum()
                && forwards == lineupType.getForwardNum();

        if (!matchesFormation) {
            throw new BadRequestException("invalid_formation",
                    "I titolari non rispettano il modulo selezionato (1 portiere, "
                            + lineupType.getDefenderNum() + " difensori, "
                            + lineupType.getMidfielderNum() + " centrocampisti, "
                            + lineupType.getForwardNum() + " attaccanti)");
        }
    }

    private List<LineupPlayerResponse> savePlayers(Lineup lineup, List<LineupPlayerRequest> players, Map<Long, TeamPlayer> teamPlayersById) {
        List<LineupPlayer> lineupPlayers = players.stream()
                .map(p -> new LineupPlayer(lineup, teamPlayersById.get(p.teamPlayerId()), p.starter()))
                .toList();

        return lineupPlayerRepository.saveAll(lineupPlayers).stream()
                .map(LineupPlayerResponse::fromEntity)
                .toList();
    }
}
