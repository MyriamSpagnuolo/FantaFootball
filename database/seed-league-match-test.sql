-- =====================================================================
-- Seed minimale per testare manualmente:
--   1) la generazione del calendario (LeagueMatchService.generateCalendar)
--      una volta che i dati (matchday aperte) arrivano da LeagueSim;
--   2) la transizione is_closed=false -> true di una giornata quando viene
--      simulata anche su LeagueSim (LeagueSimSyncService.syncMatchdays),
--      verificando che il LeagueMatch gia' generato continui a puntare
--      alla STESSA riga matchday (stesso id) senza duplicati/rotture.
--
-- Contiene app_users, league, team (il minimo per generateCalendar, che
-- legge da TeamRepository/LeagueRepository), PIU' una matchday numero 1
-- gia' aperta (is_closed = false) e un piccolo catalogo player con
-- external_id noti: servono a rendere ripetibile il test del punto 2.
-- Non tocca volutamente team_player/league_match/lineup/trade/league_invite:
-- restano a carico delle chiamate manuali che farai tu (vedi procedura sotto).
--
-- Procedura di test manuale per il punto 2:
--   a) esegui questo seed;
--   b) chiama generateCalendar() per la lega CALTEST1 (POST sull'endpoint
--      REST dedicato, con l'id di lega restituito dalla insert sotto):
--      genera i LeagueMatch del round 1 che puntano a matchday.number = 1;
--   c) su LeagueSim crea/simula la giornata numero 1 usando ESATTAMENTE gli
--      external_id inseriti qui sotto (2001-2006) per i giocatori che
--      hanno un risultato, poi marcala closed = true;
--   d) aspetta il prossimo giro di syncMatchdays() (o triggeralo a mano) e
--      verifica in DB che la riga matchday con number = 1 sia passata a
--      is_closed = true SENZA che il suo id sia cambiato e senza che siano
--      comparse righe matchday duplicate: i LeagueMatch generati al punto
--      b) devono continuare a referenziare lo stesso matchday_id.
--   Nota: se anche un solo external_id nei risultati simulati non
--   corrisponde a un player noto qui, la giornata NON verra' marcata
--   closed in locale (vedi allPlayersResolved in
--   LeagueSimMatchdayImportService) e il test non converge finche' non
--   sincronizzi anche i player mancanti (syncPlayers) o correggi gli id.
--
-- Riavviabile: svuota le tabelle coinvolte (TRUNCATE ... CASCADE, che si
-- porta via anche eventuali team_player/league_match/lineup/trade/
-- league_invite/player_results gia' presenti per gli stessi id) e resetta
-- le sequenze coinvolte.
--
-- Password in chiaro per TUTTI gli utenti di test: Fantacalcio1!
-- (rispetta i vincoli di StrongPasswordValidator: >=12 caratteri,
-- maiuscola, minuscola, cifra, carattere speciale)
-- =====================================================================

BEGIN;

TRUNCATE TABLE
    app_user_roles,
    app_users,
    league,
    team,
    matchday,
    player,
    league_match
    RESTART IDENTITY CASCADE;

ALTER SEQUENCE seq_app_users_user_id RESTART WITH 1;
ALTER SEQUENCE seq_league_id RESTART WITH 1;
ALTER SEQUENCE seq_team_id RESTART WITH 1;
ALTER SEQUENCE seq_matchday_id RESTART WITH 1;
ALTER SEQUENCE seq_player_id RESTART WITH 1;
ALTER SEQUENCE seq_league_match_id RESTART WITH 1;

-- ---------------------------------------------------------------------
-- app_users / app_user_roles
-- ---------------------------------------------------------------------
INSERT INTO app_users (username, email, password_hash, enabled) VALUES
('mario.rossi',    'mario.rossi@example.com',    '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('luigi.bianchi',  'luigi.bianchi@example.com',  '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('giovanni.verdi', 'giovanni.verdi@example.com', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('anna.russo',     'anna.russo@example.com',     '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('paolo.ferrari',  'paolo.ferrari@example.com',  '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true);

INSERT INTO app_user_roles (user_id, role)
SELECT user_id, 'USER' FROM app_users;

INSERT INTO app_user_roles (user_id, role)
SELECT user_id, 'ADMIN' FROM app_users WHERE username = 'mario.rossi';

-- ---------------------------------------------------------------------
-- league
-- ---------------------------------------------------------------------
INSERT INTO league (name, invite_code, admin_user_id, creation_date, budget) VALUES
    ('Lega Test Calendario', 'CALTEST1',
     (SELECT user_id FROM app_users WHERE username = 'mario.rossi'),
     TIMESTAMP '2026-08-01 10:00:00', 500);

-- ---------------------------------------------------------------------
-- team (5 squadre, una per utente, tutte nella stessa lega: numero dispari
-- apposta per esercitare anche il bye del round-robin)
-- ---------------------------------------------------------------------
INSERT INTO team (name, user_id, league_id, budget, total_points) VALUES
('I Bomber',             (SELECT user_id FROM app_users WHERE username = 'mario.rossi'),    (SELECT id FROM league WHERE invite_code = 'CALTEST1'), 500, 0),
('Real Fanta',           (SELECT user_id FROM app_users WHERE username = 'luigi.bianchi'),  (SELECT id FROM league WHERE invite_code = 'CALTEST1'), 500, 0),
('Gli Invincibili',      (SELECT user_id FROM app_users WHERE username = 'giovanni.verdi'), (SELECT id FROM league WHERE invite_code = 'CALTEST1'), 500, 0),
('FC Imbattibili',       (SELECT user_id FROM app_users WHERE username = 'anna.russo'),     (SELECT id FROM league WHERE invite_code = 'CALTEST1'), 500, 0),
('Atletico Fantacalcio', (SELECT user_id FROM app_users WHERE username = 'paolo.ferrari'),  (SELECT id FROM league WHERE invite_code = 'CALTEST1'), 500, 0);

-- ---------------------------------------------------------------------
-- matchday (giornata reale numero 1, ancora aperta: e' quella che
-- generateCalendar() usera' per il round 1, e quella che dovrai chiudere
-- lato LeagueSim per verificare la transizione is_closed false -> true)
-- ---------------------------------------------------------------------
INSERT INTO matchday (number, date, is_closed) VALUES
(1, DATE '2026-09-06', false);

-- ---------------------------------------------------------------------
-- player (catalogo minimo con external_id noti: usa questi stessi id per
-- i risultati che simuli su LeagueSim per la giornata 1, cosi'
-- LeagueSimMatchdayImportService trova tutti i giocatori localmente e
-- puo' marcare la giornata come chiusa)
-- ---------------------------------------------------------------------

COMMIT;
