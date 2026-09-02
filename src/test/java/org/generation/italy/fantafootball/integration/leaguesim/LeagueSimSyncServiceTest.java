package org.generation.italy.fantafootball.integration.leaguesim;

import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimMatchdayDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerResultDto;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.repositories.MatchdayRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueSimSyncServiceTest {

    @Mock
    private LeagueSimClient client;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private MatchdayRepository matchdayRepository;
    @Mock
    private LeagueSimMatchdayImportService matchdayImportService;

    private LeagueSimSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new LeagueSimSyncService(client, playerRepository, matchdayRepository, matchdayImportService);
    }

    // --- syncPlayers ---------------------------------------------------

    @Test
    void syncPlayersCreatesNewPlayerAndUpdatesExistingOneByExternalId() {
        LeagueSimPlayerDto newDto = new LeagueSimPlayerDto(10L, "Paolo", "Bianchi", "Inter", 9, "A", 25, false);
        LeagueSimPlayerDto existingDto = new LeagueSimPlayerDto(20L, "Franco", "Neri", "Milan", 4, "D", 12, true);
        when(client.fetchPlayers()).thenReturn(List.of(newDto, existingDto));

        when(playerRepository.findByExternalId(10L)).thenReturn(Optional.empty());
        Player existingPlayer = new Player(20L, "Franco", "Neri", "Milan", 4, 10, false, PlayerRole.D);
        when(playerRepository.findByExternalId(20L)).thenReturn(Optional.of(existingPlayer));

        syncService.syncPlayers();

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(2)).save(captor.capture());
        List<Player> saved = captor.getAllValues();

        Player savedNew = saved.stream()
                .filter(p -> Objects.equals(p.getExternalId(), 10L))
                .findFirst().orElseThrow();
        assertThat(savedNew.getName()).isEqualTo("Paolo");
        assertThat(savedNew.getSurname()).isEqualTo("Bianchi");
        assertThat(savedNew.getRealTeamName()).isEqualTo("Inter");
        assertThat(savedNew.getRealTeamShirtNum()).isEqualTo(9);
        assertThat(savedNew.getPrice()).isEqualTo(25);
        assertThat(savedNew.isInjured()).isFalse();
        assertThat(savedNew.getRole()).isEqualTo(PlayerRole.A);

        // Stessa istanza aggiornata (non un nuovo Player), con i campi che cambiano nel tempo aggiornati.
        assertThat(saved).contains(existingPlayer);
        assertThat(existingPlayer.getPrice()).isEqualTo(12);
        assertThat(existingPlayer.isInjured()).isTrue();
    }

    @Test
    void syncPlayersSkipsPlayerWithInvalidPositionWithoutBlockingTheOthers() {
        LeagueSimPlayerDto badDto = new LeagueSimPlayerDto(30L, "Errato", "Ruolo", "TeamX", 1, "X", 5, false);
        LeagueSimPlayerDto goodDto = new LeagueSimPlayerDto(40L, "Buono", "Ruolo", "TeamY", 2, "C", 8, false);
        when(client.fetchPlayers()).thenReturn(List.of(badDto, goodDto));
        when(playerRepository.findByExternalId(30L)).thenReturn(Optional.empty());
        when(playerRepository.findByExternalId(40L)).thenReturn(Optional.empty());

        assertThatCode(() -> syncService.syncPlayers()).doesNotThrowAnyException();

        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void syncPlayersDoesNotThrowWhenLeagueSimIsUnreachable() {
        when(client.fetchPlayers()).thenThrow(new RuntimeException("connection refused"));

        assertThatCode(() -> syncService.syncPlayers()).doesNotThrowAnyException();

        verifyNoInteractions(playerRepository);
    }

    // --- syncMatchdays -----------------------------------------------------

    @Test
    void syncMatchdaysSavesShellForMatchdayNotYetClosedOnLeagueSim() {
        LeagueSimMatchdayDto openMatchday = new LeagueSimMatchdayDto(1, LocalDate.of(2026, 3, 1), false);
        when(client.fetchMatchdays()).thenReturn(List.of(openMatchday));
        when(matchdayRepository.findByNumber(1)).thenReturn(Optional.empty());

        syncService.syncMatchdays();

        // La giornata aperta va comunque salvata in locale (number/date): serve a generateCalendar()
        // per costruire i league_match anche prima che la giornata sia stata disputata.
        ArgumentCaptor<Matchday> matchdayCaptor = ArgumentCaptor.forClass(Matchday.class);
        verify(matchdayRepository).save(matchdayCaptor.capture());
        Matchday saved = matchdayCaptor.getValue();
        assertThat(saved.getNumber()).isEqualTo(1);
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(saved.isClosed()).isFalse();

        // Non essendo chiusa, non ci sono risultati da importare.
        verify(client, never()).fetchResults(anyInt());
        verifyNoInteractions(matchdayImportService);
    }

    @Test
    void syncMatchdaysSkipsMatchdaysAlreadyImportedLocally() {
        LeagueSimMatchdayDto closedDto = new LeagueSimMatchdayDto(3, LocalDate.of(2026, 3, 1), true);
        when(client.fetchMatchdays()).thenReturn(List.of(closedDto));

        Matchday alreadyImported = new Matchday(3, LocalDate.of(2026, 3, 1));
        alreadyImported.setClosed(true);
        when(matchdayRepository.findByNumber(3)).thenReturn(Optional.of(alreadyImported));

        syncService.syncMatchdays();

        verify(client, never()).fetchResults(anyInt());
        verifyNoInteractions(matchdayImportService);
    }

    @Test
    void syncMatchdaysImportsNewlyClosedMatchday() {
        LeagueSimMatchdayDto closedDto = new LeagueSimMatchdayDto(4, LocalDate.of(2026, 3, 8), true);
        when(client.fetchMatchdays()).thenReturn(List.of(closedDto));
        when(matchdayRepository.findByNumber(4)).thenReturn(Optional.empty());

        List<LeagueSimPlayerResultDto> results = List.of(
                new LeagueSimPlayerResultDto(1L, BigDecimal.ONE, 0, 0, 0, 0, 0, 0, false, 0, false));
        when(client.fetchResults(4)).thenReturn(results);

        syncService.syncMatchdays();

        verify(matchdayImportService).importResults(closedDto, results);
    }

    @Test
    void syncMatchdaysKeepsProcessingOtherMatchdaysWhenOneFails() {
        LeagueSimMatchdayDto matchday1 = new LeagueSimMatchdayDto(1, LocalDate.of(2026, 3, 1), true);
        LeagueSimMatchdayDto matchday2 = new LeagueSimMatchdayDto(2, LocalDate.of(2026, 3, 8), true);
        when(client.fetchMatchdays()).thenReturn(List.of(matchday1, matchday2));
        when(matchdayRepository.findByNumber(1)).thenReturn(Optional.empty());
        when(matchdayRepository.findByNumber(2)).thenReturn(Optional.empty());

        when(client.fetchResults(1)).thenThrow(new RuntimeException("boom"));
        List<LeagueSimPlayerResultDto> resultsForMatchday2 = List.of();
        when(client.fetchResults(2)).thenReturn(resultsForMatchday2);

        syncService.syncMatchdays();

        // Chiamato una sola volta in totale, e proprio per la giornata 2: la giornata 1 (fallita
        // nel recupero dei risultati) non deve arrivare fino a matchdayImportService.
        verify(matchdayImportService, times(1)).importResults(any(), any());
        verify(matchdayImportService).importResults(matchday2, resultsForMatchday2);
    }

    @Test
    void syncMatchdaysDoesNotThrowWhenLeagueSimIsUnreachable() {
        when(client.fetchMatchdays()).thenThrow(new RuntimeException("down"));

        assertThatCode(() -> syncService.syncMatchdays()).doesNotThrowAnyException();

        verifyNoInteractions(matchdayImportService);
    }
}
