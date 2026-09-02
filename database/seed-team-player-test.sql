-- =====================================================================
-- Seed di assegnazione rose: assegna i player ATTUALMENTE presenti in DB
-- (tipicamente sincronizzati da LeagueSim, quindi con id/external_id
-- variabili da un ambiente all'altro) ai team della lega di test CALTEST1
-- creata da seed-league-match-test.sql. File separato apposta: quel seed
-- resetta app_users/league/team/matchday/player (RESTART IDENTITY), questo
-- invece lavora "sopra" ai dati gia' presenti senza toccarli, cosi' puoi
-- rilanciarlo ogni volta che il catalogo player cambia (nuova sync
-- LeagueSim) senza dover rigenerare anche league/team/league_match.
--
-- Prerequisiti: deve gia' esistere la lega con invite_code = 'CALTEST1'
-- coi suoi 5 team (seed-league-match-test.sql), e la tabella player deve
-- gia' contenere dei giocatori (sync LeagueSim, o il seed minimo — in tal
-- caso le quantita' sotto vanno adattate, sono pensate per un catalogo
-- ricco tipo quello reale).
--
-- Cosa fa: per ciascuna delle 4 posizioni (P/D/C/A) ordina i player per id
-- e li distribuisce round-robin sui 5 team, assegnando ad ognuno 2 P, 4 D,
-- 4 C, 2 A (12 giocatori a squadra, 10 di movimento + 1 GK titolare + 1 GK
-- di riserva): e' esattamente il minimo che serve per schierare un titolare
-- completo con lineup_type 4-4-2 (defender_num=4, midfielder_num=4,
-- foward_num=2) su uno qualsiasi dei league_match gia' generati da
-- generateCalendar(), utile per simulare il calcolo dei punteggi
-- (MatchdayCalculationService) una volta chiusa la giornata.
-- purchase_price = quotazione attuale del player (player.price); il budget
-- di ogni team viene ricalcolato di conseguenza (budget di lega - somma
-- acquisti), quindi puo' scendere sotto il budget iniziale della lega.
--
-- Riavviabile: cancella prima le righe team_player della sola lega
-- CALTEST1 (non un TRUNCATE globale, per non toccare eventuali altre
-- leghe/team_player nel DB) e le reinserisce da capo.
-- =====================================================================

BEGIN;

DELETE FROM team_player
WHERE league_id = (SELECT id FROM league WHERE invite_code = 'CALTEST1');

WITH league_teams AS (
    SELECT t.id AS team_id, t.league_id,
           ROW_NUMBER() OVER (ORDER BY t.id) AS team_rank,
           COUNT(*) OVER () AS team_count
    FROM team t
    WHERE t.league_id = (SELECT id FROM league WHERE invite_code = 'CALTEST1')
),
goalkeepers AS (
    SELECT id AS player_id, price, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM player WHERE position = 'P'
),
defenders AS (
    SELECT id AS player_id, price, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM player WHERE position = 'D'
),
midfielders AS (
    SELECT id AS player_id, price, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM player WHERE position = 'C'
),
forwards AS (
    SELECT id AS player_id, price, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM player WHERE position = 'A'
),
-- 2 portieri, 4 difensori, 4 centrocampisti, 2 attaccanti per squadra:
-- il modulo (rn - 1) % team_count distribuisce i player in giro sulle
-- squadre in ordine round-robin (1,2,3,4,5,1,2,3,4,5,...).
assignments AS (
    SELECT lt.team_id, lt.league_id, g.player_id, g.price
    FROM goalkeepers g
    JOIN league_teams lt ON lt.team_rank = ((g.rn - 1) % lt.team_count) + 1
    WHERE g.rn <= lt.team_count * 2

    UNION ALL

    SELECT lt.team_id, lt.league_id, d.player_id, d.price
    FROM defenders d
    JOIN league_teams lt ON lt.team_rank = ((d.rn - 1) % lt.team_count) + 1
    WHERE d.rn <= lt.team_count * 4

    UNION ALL

    SELECT lt.team_id, lt.league_id, m.player_id, m.price
    FROM midfielders m
    JOIN league_teams lt ON lt.team_rank = ((m.rn - 1) % lt.team_count) + 1
    WHERE m.rn <= lt.team_count * 4

    UNION ALL

    SELECT lt.team_id, lt.league_id, f.player_id, f.price
    FROM forwards f
    JOIN league_teams lt ON lt.team_rank = ((f.rn - 1) % lt.team_count) + 1
    WHERE f.rn <= lt.team_count * 2
)
INSERT INTO team_player (team_id, league_id, player_id, purchase_date, transfer_date, purchase_price)
SELECT team_id, league_id, player_id, DATE '2026-08-05', NULL, price
FROM assignments;

-- Ricalcola il budget di ogni team: budget di lega - somma dei purchase_price
-- dei suoi possessi attivi (transfer_date IS NULL), coerente col pattern gia'
-- usato in seed-data.sql.
UPDATE team t
SET budget = (SELECT budget FROM league WHERE id = t.league_id)
             - COALESCE((SELECT SUM(purchase_price) FROM team_player tp
                         WHERE tp.team_id = t.id AND tp.transfer_date IS NULL), 0)
WHERE t.league_id = (SELECT id FROM league WHERE invite_code = 'CALTEST1');

COMMIT;
