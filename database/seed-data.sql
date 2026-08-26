-- =====================================================================
-- Script di seed per test manuali/di sviluppo di FantaFootball.
--
-- Riavviabile: svuota tutte le tabelle (TRUNCATE ... CASCADE) e resetta
-- tutte le sequenze prima di ripopolare, quindi puo' essere rieseguito
-- quante volte serve per riportare il DB a uno stato noto.
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
    league_match,
    lineup,
    lineup_player,
    lineup_type,
    team,
    team_player,
    trade
    RESTART IDENTITY CASCADE;

ALTER SEQUENCE seq_app_users_user_id RESTART WITH 1;
ALTER SEQUENCE seq_league_id RESTART WITH 1;
ALTER SEQUENCE seq_league_match_id RESTART WITH 1;
ALTER SEQUENCE seq_lineup_id RESTART WITH 1;
ALTER SEQUENCE seq_team_id RESTART WITH 1;
ALTER SEQUENCE seq_team_player_id RESTART WITH 1;
ALTER SEQUENCE seq_trade_id RESTART WITH 1;

-- ---------------------------------------------------------------------
-- app_users / app_user_roles
-- ---------------------------------------------------------------------
INSERT INTO app_users (username, password_hash, enabled) VALUES
('mario.rossi',    '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('luigi.bianchi',  '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('giovanni.verdi', '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('anna.russo',     '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
('paolo.ferrari',  '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true);

INSERT INTO app_user_roles (user_id, role)
SELECT user_id, 'USER' FROM app_users;

INSERT INTO app_user_roles (user_id, role)
SELECT user_id, 'ADMIN' FROM app_users WHERE username = 'mario.rossi';

-- ---------------------------------------------------------------------
-- league
-- ---------------------------------------------------------------------
INSERT INTO league (name, invite_code, admin_user_id, creation_date, budget) VALUES
('Lega dei Campioni', 'LEGA2026',
 (SELECT user_id FROM app_users WHERE username = 'mario.rossi'),
 TIMESTAMP '2026-08-01 10:00:00', 500);

-- ---------------------------------------------------------------------
-- team (una squadra per utente, tutte nella stessa lega)
-- budget = 500 (budget di lega) - somma dei purchase_price dei giocatori
-- ---------------------------------------------------------------------
INSERT INTO team (name, user_id, league_id, budget, total_points) VALUES
('I Bomber',             (SELECT user_id FROM app_users WHERE username = 'mario.rossi'),    (SELECT id FROM league WHERE invite_code = 'LEGA2026'), 383, 52),
('Real Fanta',           (SELECT user_id FROM app_users WHERE username = 'luigi.bianchi'),  (SELECT id FROM league WHERE invite_code = 'LEGA2026'), 421, 47),
('Gli Invincibili',      (SELECT user_id FROM app_users WHERE username = 'giovanni.verdi'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'), 433, 38),
('FC Imbattibili',       (SELECT user_id FROM app_users WHERE username = 'anna.russo'),     (SELECT id FROM league WHERE invite_code = 'LEGA2026'), 419, 60),
('Atletico Fantacalcio', (SELECT user_id FROM app_users WHERE username = 'paolo.ferrari'),  (SELECT id FROM league WHERE invite_code = 'LEGA2026'), 433, 29);

-- ---------------------------------------------------------------------
-- team_player (rosa di ogni squadra)
-- ---------------------------------------------------------------------
INSERT INTO team_player (team_id, name, surname, real_team_name, real_team_shirt_num, price, is_injured, purchase_date, transfer_date, purchase_price) VALUES
-- I Bomber
((SELECT id FROM team WHERE name = 'I Bomber'),             'Alessandro', 'Ferri',      'Juventus',   1,  22, false, DATE '2026-07-01', NULL, 20),
((SELECT id FROM team WHERE name = 'I Bomber'),             'Davide',     'Conti',      'Inter',      4,  35, false, DATE '2026-07-01', NULL, 32),
((SELECT id FROM team WHERE name = 'I Bomber'),             'Matteo',     'Galli',      'Milan',      8,  28, true,  DATE '2026-07-01', NULL, 25),
((SELECT id FROM team WHERE name = 'I Bomber'),             'Simone',     'Riva',       'Napoli',     9,  45, false, DATE '2026-07-01', NULL, 40),
-- Real Fanta
((SELECT id FROM team WHERE name = 'Real Fanta'),           'Federico',   'Moretti',    'Roma',       1,  20, false, DATE '2026-07-01', NULL, 18),
((SELECT id FROM team WHERE name = 'Real Fanta'),           'Lorenzo',    'Bruno',      'Atalanta',   5,  30, false, DATE '2026-07-01', NULL, 27),
((SELECT id FROM team WHERE name = 'Real Fanta'),           'Nicolo',     'Fontana',    'Fiorentina', 10, 38, false, DATE '2026-07-01', NULL, 34),
-- Gli Invincibili
((SELECT id FROM team WHERE name = 'Gli Invincibili'),      'Andrea',     'Marino',     'Lazio',      1,  19, false, DATE '2026-07-01', NULL, 17),
((SELECT id FROM team WHERE name = 'Gli Invincibili'),      'Stefano',    'Greco',      'Torino',     3,  24, false, DATE '2026-07-01', NULL, 21),
((SELECT id FROM team WHERE name = 'Gli Invincibili'),      'Riccardo',   'Barbieri',   'Bologna',    7,  33, false, DATE '2026-07-01', NULL, 29),
-- FC Imbattibili
((SELECT id FROM team WHERE name = 'FC Imbattibili'),       'Marco',      'Villa',      'Juventus',   22, 21, false, DATE '2026-07-01', NULL, 19),
((SELECT id FROM team WHERE name = 'FC Imbattibili'),       'Luca',       'Rinaldi',    'Inter',      13, 27, false, DATE '2026-07-01', NULL, 24),
((SELECT id FROM team WHERE name = 'FC Imbattibili'),       'Gabriele',   'Costa',      'Milan',      11, 42, false, DATE '2026-07-01', NULL, 38),
-- Atletico Fantacalcio
((SELECT id FROM team WHERE name = 'Atletico Fantacalcio'), 'Emanuele',   'Longo',      'Napoli',     1,  18, false, DATE '2026-07-01', NULL, 16),
((SELECT id FROM team WHERE name = 'Atletico Fantacalcio'), 'Tommaso',    'Gatti',      'Roma',       6,  26, false, DATE '2026-07-01', NULL, 23),
((SELECT id FROM team WHERE name = 'Atletico Fantacalcio'), 'Filippo',    'De Angelis', 'Atalanta',   17, 31, false, DATE '2026-07-01', NULL, 28);

-- ---------------------------------------------------------------------
-- lineup_type (id assegnati a mano: non ha generazione automatica)
-- ---------------------------------------------------------------------
INSERT INTO lineup_type (id, defender_num, midfielder_num, foward_num) VALUES
(1, 3, 4, 3), -- 3-4-3
(2, 3, 5, 2), -- 3-5-2
(3, 4, 3, 3), -- 4-3-3
(4, 4, 4, 2), -- 4-4-2
(5, 4, 5, 1), -- 4-5-1
(6, 5, 3, 2), -- 5-3-2
(7, 5, 4, 1); -- 5-4-1

-- ---------------------------------------------------------------------
-- league_match
-- Giornata 1 (23/08): gia' giocata, con punteggi. Atletico Fantacalcio riposa.
-- Giornata 2 (30/08): non ancora giocata (punteggi NULL). I Bomber riposa.
-- ---------------------------------------------------------------------
INSERT INTO league_match (league_id, home_team_id, away_team_id, home_score, away_score, home_goals, away_goals, match_day) VALUES
((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM team WHERE name = 'Real Fanta'),
 68.5, 54.0, 2, 1, TIMESTAMP '2026-08-23 15:00:00'),
((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM team WHERE name = 'Gli Invincibili'), (SELECT id FROM team WHERE name = 'FC Imbattibili'),
 45.0, 72.5, 0, 3, TIMESTAMP '2026-08-23 15:00:00'),
((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM team WHERE name = 'Real Fanta'), (SELECT id FROM team WHERE name = 'Gli Invincibili'),
 NULL, NULL, NULL, NULL, TIMESTAMP '2026-08-30 15:00:00'),
((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM team WHERE name = 'FC Imbattibili'), (SELECT id FROM team WHERE name = 'Atletico Fantacalcio'),
 NULL, NULL, NULL, NULL, TIMESTAMP '2026-08-30 15:00:00');

-- ---------------------------------------------------------------------
-- lineup (solo per le partite gia' giocate di giornata 1)
-- ---------------------------------------------------------------------
INSERT INTO lineup (team_id, league_match_id, is_defensive, lineup_type_id) VALUES
((SELECT id FROM team WHERE name = 'I Bomber'),
 (SELECT id FROM league_match WHERE home_team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND away_team_id = (SELECT id FROM team WHERE name = 'Real Fanta')),
 false, 4), -- 4-4-2
((SELECT id FROM team WHERE name = 'Real Fanta'),
 (SELECT id FROM league_match WHERE home_team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND away_team_id = (SELECT id FROM team WHERE name = 'Real Fanta')),
 true, 6), -- 5-3-2, formazione difensiva
((SELECT id FROM team WHERE name = 'Gli Invincibili'),
 (SELECT id FROM league_match WHERE home_team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili') AND away_team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')),
 false, 3), -- 4-3-3
((SELECT id FROM team WHERE name = 'FC Imbattibili'),
 (SELECT id FROM league_match WHERE home_team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili') AND away_team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')),
 false, 1); -- 3-4-3

-- ---------------------------------------------------------------------
-- lineup_player
-- NB: player_id non e' una foreign key verso team_player (colonna libera
-- rimasta dal vecchio modello): sono valori segnaposto, non collegati
-- alla rosa reale in team_player.
-- ---------------------------------------------------------------------
-- I Bomber: 11 titolari + 2 riserve
INSERT INTO lineup_player (lineup_id, player_id, starter, position) VALUES
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 101, true, 'P'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 102, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 103, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 104, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 105, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 106, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 107, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 108, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 109, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 110, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 111, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 112, false, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')), 113, false, 'A');

-- Real Fanta: 11 titolari (5-3-2), nessuna riserva
INSERT INTO lineup_player (lineup_id, player_id, starter, position) VALUES
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 201, true, 'P'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 202, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 203, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 204, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 205, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 206, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 207, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 208, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 209, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 210, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')), 211, true, 'A');

-- Gli Invincibili: 11 titolari (4-3-3)
INSERT INTO lineup_player (lineup_id, player_id, starter, position) VALUES
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 301, true, 'P'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 302, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 303, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 304, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 305, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 306, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 307, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 308, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 309, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 310, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')), 311, true, 'A');

-- FC Imbattibili: 11 titolari (3-4-3) + 3 riserve
INSERT INTO lineup_player (lineup_id, player_id, starter, position) VALUES
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 401, true, 'P'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 402, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 403, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 404, true, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 405, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 406, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 407, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 408, true, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 409, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 410, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 411, true, 'A'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 412, false, 'D'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 413, false, 'C'),
((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')), 414, false, 'A');

-- ---------------------------------------------------------------------
-- trade: una per stato (PENDING / ACCEPTED / REJECTED), con importi che
-- coprono i tre casi: offerta di denaro extra, richiesta di denaro, nessun denaro.
-- ---------------------------------------------------------------------
INSERT INTO trade (proposing_team_id, receiving_team_id, trade_player_id, offered_player_id, amount, status, proposal_date) VALUES
-- I Bomber offre Galli + 20 crediti per avere Fontana da Real Fanta (PENDING)
((SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM team WHERE name = 'Real Fanta'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta') AND name = 'Nicolo' AND surname = 'Fontana'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND name = 'Matteo' AND surname = 'Galli'),
 20, 'PENDING', TIMESTAMP '2026-08-24 09:00:00'),

-- Gli Invincibili offre Barbieri e chiede 15 crediti indietro per avere Costa da FC Imbattibili (ACCEPTED)
((SELECT id FROM team WHERE name = 'Gli Invincibili'), (SELECT id FROM team WHERE name = 'FC Imbattibili'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili') AND name = 'Gabriele' AND surname = 'Costa'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili') AND name = 'Riccardo' AND surname = 'Barbieri'),
 -15, 'ACCEPTED', TIMESTAMP '2026-08-22 18:30:00'),

-- FC Imbattibili offre Rinaldi (nessun denaro) per avere De Angelis da Atletico Fantacalcio (REJECTED)
((SELECT id FROM team WHERE name = 'FC Imbattibili'), (SELECT id FROM team WHERE name = 'Atletico Fantacalcio'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Atletico Fantacalcio') AND name = 'Filippo' AND surname = 'De Angelis'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili') AND name = 'Luca' AND surname = 'Rinaldi'),
 NULL, 'REJECTED', TIMESTAMP '2026-08-25 12:00:00');

COMMIT;
