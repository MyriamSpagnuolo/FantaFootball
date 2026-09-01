package org.generation.italy.fantafootball.integration.leaguesim;

import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimMatchdayDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerResultDto;
import org.generation.italy.fantafootball.model.entities.Matchday;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerResult;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.generation.italy.fantafootball.model.repositories.MatchdayRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.generation.italy.fantafootball.model.repositories.PlayerResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueSimMatchdayImportServiceTest {

    @Mock
    private MatchdayRepository matchdayRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerResultRepository playerResultRepository;

    private LeagueSimMatchdayImportService importService;

    @BeforeEach
    void setUp() {
        importService = new LeagueSimMatchdayImportService(matchdayRepository, playerRepository, playerResultRepository);
        // matchdayRepository.save simula la persistenza restituendo lo stesso oggetto passato:
        // ci basta per verificare cosa il service imposta sull'entita', non serve un vero id generato.
        when(matchdayRepository.save(any(Matchday.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void savesAllResultsAndClosesMatchdayWhenEveryPlayerIsResolved() {
        LeagueSimMatchdayDto matchdayDto = new LeagueSimMatchdayDto(5, LocalDate.of(2026, 3, 1), true);
        when(matchdayRepository.findByNumber(5)).thenReturn(Optional.empty());

        Player player1 = new Player(100L, "Mario", "Rossi", "Roma", 10, 20, false, PlayerRole.A);
        Player player2 = new Player(200L, "Luigi", "Verdi", "Lazio", 5, 15, false, PlayerRole.D);
        when(playerRepository.findByExternalId(100L)).thenReturn(Optional.of(player1));
        when(playerRepository.findByExternalId(200L)).thenReturn(Optional.of(player2));
        when(playerResultRepository.findByPlayerIdAndMatchdayId(any(), any())).thenReturn(Optional.empty());

        // Valori tutti diversi tra loro apposta: se il mapping scambiasse due campi (es. assist con
        // autogol) un valore ripetuto (es. tutti zero) non lo farebbe emergere in un assert.
        LeagueSimPlayerResultDto result1 = new LeagueSimPlayerResultDto(
                100L, new BigDecimal("6.50"), 2, 1, 3, 4, 5, 6, true, 7, true);
        LeagueSimPlayerResultDto result2 = new LeagueSimPlayerResultDto(
                200L, new BigDecimal("5.00"), 0, 0, 0, 0, 0, 0, false, 0, false);

        importService.importResults(matchdayDto, List.of(result1, result2));

        ArgumentCaptor<PlayerResult> resultCaptor = ArgumentCaptor.forClass(PlayerResult.class);
        verify(playerResultRepository, times(2)).save(resultCaptor.capture());
        PlayerResult savedForPlayer1 = resultCaptor.getAllValues().stream()
                .filter(r -> r.getPlayer() == player1)
                .findFirst()
                .orElseThrow();

        assertThat(savedForPlayer1.getRating()).isEqualByComparingTo("6.50");
        assertThat(savedForPlayer1.getGoalNum()).isEqualTo(2);
        assertThat(savedForPlayer1.getGoalConceded()).isEqualTo(1);
        assertThat(savedForPlayer1.getAutogoalNum()).isEqualTo(3);
        assertThat(savedForPlayer1.getAssistNum()).isEqualTo(4);
        assertThat(savedForPlayer1.getPenaltySaved()).isEqualTo(5);
        assertThat(savedForPlayer1.getPenaltyFailed()).isEqualTo(6);
        assertThat(savedForPlayer1.getCleanSheet()).isTrue();
        assertThat(savedForPlayer1.getYellowCard()).isEqualTo(7);
        assertThat(savedForPlayer1.isRedCard()).isTrue();

        ArgumentCaptor<Matchday> matchdayCaptor = ArgumentCaptor.forClass(Matchday.class);
        verify(matchdayRepository, times(2)).save(matchdayCaptor.capture());
        Matchday lastSaved = matchdayCaptor.getAllValues().get(matchdayCaptor.getAllValues().size() - 1);
        assertThat(lastSaved.isClosed()).isTrue();
    }

    @Test
    void skipsUnknownPlayerAndLeavesMatchdayOpenForRetry() {
        LeagueSimMatchdayDto matchdayDto = new LeagueSimMatchdayDto(9, LocalDate.of(2026, 3, 8), true);
        when(matchdayRepository.findByNumber(9)).thenReturn(Optional.empty());
        // Nessun Player locale con questo externalId: simula un giocatore non ancora sincronizzato.
        when(playerRepository.findByExternalId(300L)).thenReturn(Optional.empty());

        LeagueSimPlayerResultDto resultDto = new LeagueSimPlayerResultDto(
                300L, new BigDecimal("6.0"), 0, 0, 0, 0, 0, 0, false, 0, false);

        importService.importResults(matchdayDto, List.of(resultDto));

        verify(playerResultRepository, never()).save(any());
        // matchdayRepository.save viene chiamato una sola volta (per assicurarsi l'id iniziale):
        // il secondo save, quello che marca closed = true, non deve avvenire.
        verify(matchdayRepository, times(1)).save(any(Matchday.class));
    }

    @Test
    void reimportingSameResultUpdatesExistingRowInsteadOfDuplicating() {
        Matchday existingMatchday = new Matchday(7, LocalDate.of(2026, 3, 15));
        when(matchdayRepository.findByNumber(7)).thenReturn(Optional.of(existingMatchday));

        Player player = new Player(400L, "Nome", "Cognome", "TeamX", 1, 10, false, PlayerRole.C);
        when(playerRepository.findByExternalId(400L)).thenReturn(Optional.of(player));

        PlayerResult existingResult = new PlayerResult();
        when(playerResultRepository.findByPlayerIdAndMatchdayId(any(), any())).thenReturn(Optional.of(existingResult));

        LeagueSimMatchdayDto matchdayDto = new LeagueSimMatchdayDto(7, LocalDate.of(2026, 3, 15), true);
        LeagueSimPlayerResultDto resultDto = new LeagueSimPlayerResultDto(
                400L, new BigDecimal("7.00"), 1, 0, 0, 0, 0, 0, false, 0, false);

        importService.importResults(matchdayDto, List.of(resultDto));

        // Stessa istanza salvata: e' un aggiornamento in place, non la creazione di un duplicato.
        verify(playerResultRepository).save(existingResult);
        assertThat(existingResult.getRating()).isEqualByComparingTo("7.00");
    }
}
