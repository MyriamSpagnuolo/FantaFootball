-- =====================================================================
-- Seed per testare manualmente (frontend o Swagger) il flusso di
-- schieramento della formazione (POST/PUT/GET
-- /api/teams/{teamId}/matches/{leagueMatchId}/lineup), con rose gia'
-- pronte composte da PLAYER REALI (quelli sincronizzati da LeagueSim
-- in tabella player, NON un catalogo fittizio inserito da questo script).
--
-- A differenza di seed-data.sql / seed-league-match-test.sql /
-- seed-asta-simulator.sql, questo script NON fa TRUNCATE globale:
-- gira "sopra" un DB che ha gia' dati reali (utente HEAD reale in
-- app_users, catalogo player reale sincronizzato da LeagueSim, matchday
-- reali, lineup_type gia' seedata). Toccare quelle tabelle con una
-- TRUNCATE le cancellerebbe. Questo script tocca SOLO la propria lega
-- di test (invite_code = 'LINEUPQA1') e i suoi 6 utenti fittizi.
--
-- Cosa crea:
--   - 6 app_users fittizi + relativo ruolo globale USER (il ruolo
--     globale non serve per l'admin di lega, vedi CLAUDE.md: l'admin di
--     lega e' league.admin_user_id, non UserRole)
--   - 1 league (budget lega = 500 crediti/team), 6 team (1 per utente)
--   - Le rose: per ciascuna squadra 2 portieri, 6 difensori, 6
--     centrocampisti, 4 attaccanti (18 giocatori), presi dal catalogo
--     player REALE gia' in DB. E' piu' di un titolare completo per
--     ciascuno dei 7 lineup_type standard (che richiedono al massimo
--     5 D / 5 C / 3 A), quindi ogni squadra puo' schierare qualsiasi
--     modulo e ha sempre almeno una riserva per ruolo di movimento.
--   - Selezione player con "snake draft" per prezzo (round 1: team
--     1..6, round 2: team 6..1, ecc., saltando i 6 player piu' cari di
--     ogni ruolo per lasciare un margine): tiene il budget speso di
--     ogni squadra bilanciato (nessuna sopra i 500 crediti di lega) e
--     comunque alto, cosi' resta "poco" credito residuo per squadra —
--     verificato empiricamente sul catalogo attuale (~430-450 spesi su
--     500, quindi 50-70 crediti residui a squadra). Con un catalogo
--     player diverso (altra sincronizzazione LeagueSim) i numeri
--     assoluti cambiano ma la logica si riadatta da sola: prende sempre
--     "i migliori disponibili esclusi i piu' cari", quindi il credito
--     residuo resta comunque una piccola frazione del budget totale.
--   - Non tutti i 500 player reali vengono assegnati: solo 18 * 6 = 108,
--     gli altri restano liberi (nessun vincolo di unicita' violato).
--   - NESSUN league_match: a differenza di una vecchia versione di questo
--     script, NON inseriamo piu' a mano 3 partite fittizie di round 1.
--     Farlo bypassava del tutto LeagueMatchService.generateCalendar(),
--     che invece genera round-robin (RoundRobinScheduler) un numero di
--     round pari a matchdayRepository.findByClosedFalseOrderByDateAsc()
--     — cioe' dipendente da QUANTE giornate reali ancora aperte risultano
--     sincronizzate da LeagueSim in quel momento. Il calendario va quindi
--     generato con la vera API (POST /api/leagues/{leagueId}/matches,
--     solo admin di lega — vedi step 4 della procedura sotto), cosi' il
--     test copre anche quella logica invece di scavalcarla.
--   - NESSUNA lineup/lineup_player: e' esattamente cio' che l'utente
--     deve testare a mano (frontend o Swagger) dopo aver lanciato lo
--     script.
--
-- Riavviabile: la BEGIN qui sotto cancella prima ogni dato della SOLA
-- lega 'LINEUPQA1' (lineup -> league_match -> team_player -> team ->
-- league, nell'ordine richiesto dalle FK RESTRICT), poi i 6 utenti
-- fittizzi, e reinserisce tutto da capo. In particolare la lineup
-- (e a cascata lineup_player, FK ON DELETE CASCADE) viene sempre
-- azzerata per prima: e' questo che permette di rilanciare lo script e
-- ripetere il test di creazione formazione via API senza incappare nel
-- 409 lineup_already_exists di LineupService.createLineup. Non tocca
-- mai app_users diversi da questi 6, ne' player/lineup_type.
--
-- matchday / player_results: a differenza delle altre tabelle sono
-- condivise da TUTTA fantafootball (non scoped a 'LINEUPQA1'). Lo script
-- cancella TUTTI i player_results, poi le matchday non piu' referenziate
-- da nessun league_match (di QUALSIASI lega, non solo la nostra —
-- fk_lm_matchday in ddl.sql e' NO ACTION, quindi una matchday ancora
-- usata da un'altra lega non verrebbe cancellata: la DELETE fallirebbe e
-- farebbe rollback dell'intero script, non corrompe nulla). Reset totale
-- voluto, non solo delle giornate "orfane": serve quando anche LeagueSim
-- (l'altro servizio) viene fatto ripartire dalla prima giornata, per
-- evitare che i vecchi player_results/matchday locali restino sfalzati
-- rispetto ai NUOVI dati che LeagueSim restituira' per quegli stessi
-- number di giornata. Al giro successivo di
-- LeagueSimSyncService.syncMatchdays() (schedulato, non lanciato da
-- questo script) matchday/player_results vengono reimportati ex novo
-- dallo stato corrente di LeagueSim.
--
-- Password in chiaro per TUTTI gli utenti di test: Fantacalcio1!
-- (rispetta i vincoli di StrongPasswordValidator: >=12 caratteri,
-- maiuscola, minuscola, cifra, carattere speciale)
--
-- Procedura di test manuale suggerita, dopo aver lanciato lo script:
--   1) POST /api/auth/login con uno degli username sotto + la password
--      sopra, prendi il JWT restituito;
--   2) GET /api/teams/me (con quel JWT) per trovare il teamId
--      dell'utente;
--   3) GET /api/teams/{teamId}/players per la rosa (18 player, con
--      teamPlayerId, position, price) e GET /api/lineup-types per gli
--      id dei moduli disponibili;
--   4) POST /api/leagues/{leagueId}/matches (leagueId della lega
--      'LINEUPQA1', solo con il JWT di lineup.test1 che e' l'admin di
--      lega) per generare il calendario vero tramite
--      LeagueMatchService.generateCalendar() — genera tanti round quante
--      sono le matchday reali ancora aperte in quel momento, con gli
--      accoppiamenti round-robin. Se non c'e' nessuna matchday aperta in
--      DB (sincronizzata da LeagueSim), fallisce con 400 invalid_rounds:
--      serve almeno una giornata reale aperta prima di lanciare lo step.
--      Lo script stesso ha appena azzerato matchday/player_results (vedi
--      sopra): il DB resta senza matchday finche' non arriva il prossimo
--      giro schedulato di LeagueSimSyncService.syncMatchdays()
--      (intervallo leaguesim.results-sync-interval) — aspettalo prima di
--      chiamare questo step.
--   5) trova il leagueMatchId di round 1 per la propria squadra nella
--      risposta dello step 4 (o GET /api/leagues/{leagueId}/matches);
--   6) POST /api/teams/{teamId}/matches/{leagueMatchId}/lineup con un
--      LineupRequest {lineupTypeId, defensive, players:[{teamPlayerId,
--      starter}, ...]} — 1 portiere + D/C/A del modulo scelto tra gli
--      starter=true, il resto (se presente) starter=false in panchina.
--   7) rilancia lo script per azzerare formazione e calendario e
--      ripetere il test dallo step 4.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- Reset scoped alla sola lega 'LINEUPQA1' (nell'ordine richiesto dalle
-- FK RESTRICT: lineup prima di league_match, team_player dopo lineup
-- perche' lineup_player referenzia team_player).
-- ---------------------------------------------------------------------
DELETE FROM lineup
WHERE team_id IN (SELECT id FROM team WHERE league_id = (SELECT id FROM league WHERE invite_code = 'LINEUPQA1'));

DELETE FROM league_match
WHERE league_id = (SELECT id FROM league WHERE invite_code = 'LINEUPQA1');

DELETE FROM team_player
WHERE league_id = (SELECT id FROM league WHERE invite_code = 'LINEUPQA1');

DELETE FROM team
WHERE league_id = (SELECT id FROM league WHERE invite_code = 'LINEUPQA1');

DELETE FROM league
WHERE invite_code = 'LINEUPQA1';

-- Vedi commento in testa al file: reset totale (globale, non scoped alla
-- lega di test) di player_results e matchday, per riallinearsi a
-- LeagueSim quando anche li' la stagione viene fatta ripartire dalla
-- prima giornata. player_results va cancellato PRIMA di matchday (FK
-- fk_player_results_matchday NO ACTION la blocca finche' ci sono righe
-- agganciate).
DELETE FROM player_results;

-- Le matchday restano cancellabili solo se nessun'altra lega le
-- referenzia ancora via league_match (fk_lm_matchday e' NO ACTION): se
-- nel DB c'e' un'altra lega con calendario gia' generato su queste
-- giornate, questa DELETE fallisce e fa rollback dell'intero script
-- invece di corrompere silenziosamente quell'altra lega.
DELETE FROM matchday m
WHERE NOT EXISTS (SELECT 1 FROM league_match lm WHERE lm.matchday_id = m.id);

DELETE FROM app_users
WHERE username IN ('lineup.test1', 'lineup.test2', 'lineup.test3', 'lineup.test4', 'lineup.test5', 'lineup.test6');
-- app_user_roles ha ON DELETE CASCADE da app_users: nessuna DELETE dedicata necessaria.

-- ---------------------------------------------------------------------
-- app_users / app_user_roles (fittizi, solo per questa lega di test)
-- ---------------------------------------------------------------------
INSERT INTO app_users (username, email, password_hash, enabled) VALUES
('lineup.test1', 'lineup.test1@example.com', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('lineup.test2', 'lineup.test2@example.com', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('lineup.test3', 'lineup.test3@example.com', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('lineup.test4', 'lineup.test4@example.com', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('lineup.test5', 'lineup.test5@example.com', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('lineup.test6', 'lineup.test6@example.com', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true);

INSERT INTO app_user_roles (user_id, role)
SELECT user_id, 'USER' FROM app_users
WHERE username IN ('lineup.test1', 'lineup.test2', 'lineup.test3', 'lineup.test4', 'lineup.test5', 'lineup.test6');

-- ---------------------------------------------------------------------
-- league (lineup.test1 e' l'admin di lega, non serve ruolo globale ADMIN)
-- ---------------------------------------------------------------------
INSERT INTO league (name, invite_code, admin_user_id, creation_date, budget) VALUES
    ('Lega Test Formazioni', 'LINEUPQA1',
     (SELECT user_id FROM app_users WHERE username = 'lineup.test1'),
     CURRENT_TIMESTAMP, 500);

-- ---------------------------------------------------------------------
-- team (6 squadre, una per utente fittizio, tutte nella nuova lega)
-- ---------------------------------------------------------------------
INSERT INTO team (name, user_id, league_id, budget, total_points) VALUES
('Squadra Alfa',    (SELECT user_id FROM app_users WHERE username = 'lineup.test1'), (SELECT id FROM league WHERE invite_code = 'LINEUPQA1'), 500, 0),
('Squadra Beta',    (SELECT user_id FROM app_users WHERE username = 'lineup.test2'), (SELECT id FROM league WHERE invite_code = 'LINEUPQA1'), 500, 0),
('Squadra Gamma',   (SELECT user_id FROM app_users WHERE username = 'lineup.test3'), (SELECT id FROM league WHERE invite_code = 'LINEUPQA1'), 500, 0),
('Squadra Delta',   (SELECT user_id FROM app_users WHERE username = 'lineup.test4'), (SELECT id FROM league WHERE invite_code = 'LINEUPQA1'), 500, 0),
('Squadra Epsilon', (SELECT user_id FROM app_users WHERE username = 'lineup.test5'), (SELECT id FROM league WHERE invite_code = 'LINEUPQA1'), 500, 0),
('Squadra Zeta',    (SELECT user_id FROM app_users WHERE username = 'lineup.test6'), (SELECT id FROM league WHERE invite_code = 'LINEUPQA1'), 500, 0);

-- ---------------------------------------------------------------------
-- team_player: assegna ai 6 team, per ciascun ruolo, i player REALI
-- gia' presenti in tabella player, con uno "snake draft" per prezzo
-- decrescente (saltando i 6 piu' cari di ogni ruolo, per lasciare un
-- margine di sicurezza sotto il budget di lega). 2 P / 6 D / 6 C / 4 A
-- a squadra: copre comodamente qualunque lineup_type (max 5 D/5 C/3 A)
-- con almeno una riserva per ruolo di movimento.
-- ---------------------------------------------------------------------
WITH league_teams AS (
    SELECT t.id AS team_id, t.league_id, ROW_NUMBER() OVER (ORDER BY t.id) AS team_rank
    FROM team t
    WHERE t.league_id = (SELECT id FROM league WHERE invite_code = 'LINEUPQA1')
),
goalkeepers AS (
    SELECT id, price, ROW_NUMBER() OVER (ORDER BY price DESC, id) AS rn FROM player WHERE position = 'P'
),
defenders AS (
    SELECT id, price, ROW_NUMBER() OVER (ORDER BY price DESC, id) AS rn FROM player WHERE position = 'D'
),
midfielders AS (
    SELECT id, price, ROW_NUMBER() OVER (ORDER BY price DESC, id) AS rn FROM player WHERE position = 'C'
),
forwards AS (
    SELECT id, price, ROW_NUMBER() OVER (ORDER BY price DESC, id) AS rn FROM player WHERE position = 'A'
),
-- rn2 riparte da 1 dopo aver scartato i primi 6 (i piu' cari); il modulo
-- (rn2-1)/6 e' il "giro" di draft, pari => 1..6, dispari => 6..1 (snake).
gk_picks AS (
    SELECT id AS player_id, price,
           CASE WHEN (((rn - 7) / 6) % 2 = 0) THEN (((rn - 7) % 6) + 1) ELSE (6 - ((rn - 7) % 6)) END AS team_rank
    FROM goalkeepers WHERE rn BETWEEN 7 AND 18
),
def_picks AS (
    SELECT id AS player_id, price,
           CASE WHEN (((rn - 7) / 6) % 2 = 0) THEN (((rn - 7) % 6) + 1) ELSE (6 - ((rn - 7) % 6)) END AS team_rank
    FROM defenders WHERE rn BETWEEN 7 AND 42
),
mid_picks AS (
    SELECT id AS player_id, price,
           CASE WHEN (((rn - 7) / 6) % 2 = 0) THEN (((rn - 7) % 6) + 1) ELSE (6 - ((rn - 7) % 6)) END AS team_rank
    FROM midfielders WHERE rn BETWEEN 7 AND 42
),
fwd_picks AS (
    SELECT id AS player_id, price,
           CASE WHEN (((rn - 7) / 6) % 2 = 0) THEN (((rn - 7) % 6) + 1) ELSE (6 - ((rn - 7) % 6)) END AS team_rank
    FROM forwards WHERE rn BETWEEN 7 AND 30
),
all_picks AS (
    SELECT * FROM gk_picks
    UNION ALL SELECT * FROM def_picks
    UNION ALL SELECT * FROM mid_picks
    UNION ALL SELECT * FROM fwd_picks
)
INSERT INTO team_player (team_id, league_id, player_id, purchase_date, transfer_date, purchase_price)
SELECT lt.team_id, lt.league_id, ap.player_id, DATE '2026-09-01', NULL, ap.price
FROM all_picks ap
JOIN league_teams lt ON lt.team_rank = ap.team_rank;

-- Ricalcola il budget di ogni team: budget di lega - somma dei
-- purchase_price dei suoi possessi attivi (pattern gia' usato in
-- seed-data.sql / seed-team-player-test.sql).
UPDATE team t
SET budget = (SELECT budget FROM league WHERE id = t.league_id)
             - COALESCE((SELECT SUM(purchase_price) FROM team_player tp
                         WHERE tp.team_id = t.id AND tp.transfer_date IS NULL), 0)
WHERE t.league_id = (SELECT id FROM league WHERE invite_code = 'LINEUPQA1');

-- ---------------------------------------------------------------------
-- Nessun league_match qui: va generato con la vera API (POST
-- /api/leagues/{leagueId}/matches, come admin di lega lineup.test1) in
-- modo che LeagueMatchService.generateCalendar() giri per davvero sulle
-- matchday reali ancora aperte al momento — vedi step 4 della procedura
-- di test qui sopra.
-- ---------------------------------------------------------------------

COMMIT;
