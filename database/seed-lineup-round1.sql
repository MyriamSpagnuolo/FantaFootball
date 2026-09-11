-- =====================================================================
-- Schiera automaticamente le formazioni di round 1 (giornata di lega 1)
-- per TUTTE le squadre della lega di test 'LINEUPQA1' (vedi
-- seed-lineup-test.sql), cosi' non serve farlo a mano via Swagger per
-- ogni squadra solo per arrivare a testare il calcolo del punteggio.
--
-- Prerequisiti, in questo ordine:
--   1) seed-lineup-test.sql gia' lanciato (6 team, rose da 2 P / 6 D /
--      6 C / 4 A ciascuno, presi dal catalogo player reale);
--   2) calendario GIA' generato con la vera API
--      (POST /api/leagues/{leagueId}/matches, JWT di lineup.test1 che
--      e' l'admin della lega 'LINEUPQA1') — questo script NON genera il
--      calendario, legge solo i league_match di round_number = 1 gia'
--      creati da LeagueMatchService.generateCalendar(). Se li' non c'e'
--      ancora nessuna giornata reale aperta sincronizzata da LeagueSim,
--      quella chiamata fallisce con 400 invalid_rounds: vedi
--      seed-lineup-test.sql per i dettagli.
--
-- Cosa fa:
--   - Trova i 3 league_match di round_number = 1 della lega 'LINEUPQA1'
--     (qualunque sia l'abbinamento home/away uscito dal round-robin —
--     non li assume fissi come faceva la vecchia versione di
--     seed-lineup-test.sql).
--   - Per ciascuna delle 6 squadre coinvolte, crea una lineup con modulo
--     4-3-3 (lineup_type_id = 3: 4 difensori, 3 centrocampisti, 3
--     attaccanti — scelto perche' qualunque rosa da seed-lineup-test.sql,
--     che ha sempre almeno 4 D / 3 C / 3 A disponibili, lo soddisfa
--     comodamente senza dover controllare rosa per rosa).
--   - Titolari: dalla rosa attiva della squadra (team_player con
--     transfer_date IS NULL), il portiere piu' "vecchio" per id e i
--     primi 4 D / 3 C / 3 A per id (ordine arbitrario ma deterministico,
--     non ha alcun significato calcistico). Il resto della rosa (1 P, 2
--     D, 3 C, 1 A) va in panchina (starter = false): stessa cardinalita'
--     della rosa intera (18), nessun giocatore escluso.
--   - is_defensive = false per tutte (il modificatore difesa, vedi
--     calculateMatchday/PROJECT_CONTEXT.md, qui e' irrilevante: e' solo
--     per testare il flusso di calcolo, non per validarne l'esito).
--
-- Riavviabile: cancella prima le lineup gia' presenti per quei 3
-- league_match (lineup_player a cascata, FK fk_lp_lineup ON DELETE
-- CASCADE), cosi' si puo' rilanciare piu' volte senza incappare nel
-- vincolo uq_lineup (team_id, league_match_id).
--
-- NON tocca altre giornate/round, altre leghe, ne' matchday/player.
-- =====================================================================

BEGIN;

-- Guardia esplicita: meglio un errore chiaro qui che 0 righe inserite in
-- silenzio se il calendario non e' ancora stato generato.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM league_match lm
        JOIN league lg ON lg.id = lm.league_id
        WHERE lg.invite_code = 'LINEUPQA1' AND lm.round_number = 1
    ) THEN
        RAISE EXCEPTION 'Nessun league_match di round 1 per la lega LINEUPQA1: genera prima il calendario con POST /api/leagues/{leagueId}/matches (admin di lega lineup.test1)';
    END IF;
END $$;

DELETE FROM lineup
WHERE league_match_id IN (
    SELECT lm.id
    FROM league_match lm
    JOIN league lg ON lg.id = lm.league_id
    WHERE lg.invite_code = 'LINEUPQA1' AND lm.round_number = 1
);

WITH round1_pairs AS (
    -- Una riga per squadra coinvolta in round 1 (home + away di ogni partita).
    SELECT lm.id AS league_match_id, lm.home_team_id AS team_id
    FROM league_match lm
    JOIN league lg ON lg.id = lm.league_id
    WHERE lg.invite_code = 'LINEUPQA1' AND lm.round_number = 1
    UNION ALL
    SELECT lm.id AS league_match_id, lm.away_team_id AS team_id
    FROM league_match lm
    JOIN league lg ON lg.id = lm.league_id
    WHERE lg.invite_code = 'LINEUPQA1' AND lm.round_number = 1
),
inserted_lineup AS (
    INSERT INTO lineup (team_id, league_match_id, is_defensive, lineup_type_id)
    SELECT team_id, league_match_id, false, 3 -- 3 = modulo 4-3-3 (vedi lineup_type)
    FROM round1_pairs
    RETURNING id, team_id
),
ranked_roster AS (
    -- Rosa attiva di ogni squadra coinvolta, con un rank per ruolo (per
    -- decidere deterministicamente chi e' titolare e chi in panchina).
    SELECT tp.id AS team_player_id, tp.team_id, p."position",
           ROW_NUMBER() OVER (PARTITION BY tp.team_id, p."position" ORDER BY tp.id) AS rn
    FROM team_player tp
    JOIN player p ON p.id = tp.player_id
    WHERE tp.transfer_date IS NULL
      AND tp.team_id IN (SELECT team_id FROM round1_pairs)
),
roster_with_starter_flag AS (
    SELECT team_player_id, team_id,
           CASE
               WHEN "position" = 'P' AND rn <= 1 THEN true
               WHEN "position" = 'D' AND rn <= 4 THEN true
               WHEN "position" = 'C' AND rn <= 3 THEN true
               WHEN "position" = 'A' AND rn <= 3 THEN true
               ELSE false
           END AS starter
    FROM ranked_roster
)
INSERT INTO lineup_player (lineup_id, player_id, starter)
SELECT il.id, r.team_player_id, r.starter
FROM inserted_lineup il
JOIN roster_with_starter_flag r ON r.team_id = il.team_id;

COMMIT;
