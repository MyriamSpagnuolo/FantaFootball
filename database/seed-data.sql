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
    league_invite,
    league_match,
    lineup,
    lineup_player,
    lineup_type,
    matchday,
    player,
    player_results,
    team,
    team_player,
    trade
    RESTART IDENTITY CASCADE;

ALTER SEQUENCE seq_app_users_user_id RESTART WITH 1;
ALTER SEQUENCE seq_league_id RESTART WITH 1;
ALTER SEQUENCE seq_league_invite_id RESTART WITH 1;
ALTER SEQUENCE seq_league_match_id RESTART WITH 1;
ALTER SEQUENCE seq_lineup_id RESTART WITH 1;
ALTER SEQUENCE seq_matchday_id RESTART WITH 1;
ALTER SEQUENCE seq_player_id RESTART WITH 1;
ALTER SEQUENCE seq_player_results_id RESTART WITH 1;
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
                                                             ('paolo.ferrari',  '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true),
                                                             ('sara.gallo',      '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji', true);

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
-- league_invite: gli inviti ACCEPTED corrispondono ai 4 team creati dagli
-- utenti invitati (mario.rossi e' l'admin che ha creato la lega, non
-- serve un invito per il suo stesso team); sara.gallo ha invece un
-- invito ancora PENDING, non fa parte di nessun team.
-- ---------------------------------------------------------------------
INSERT INTO league_invite (league_id, invited_by_user_id, invited_user_id, status, sent_date, response_date) VALUES
                                                                                                                 ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                  (SELECT user_id FROM app_users WHERE username = 'mario.rossi'), (SELECT user_id FROM app_users WHERE username = 'luigi.bianchi'),
                                                                                                                  'ACCEPTED', TIMESTAMP '2026-08-01 10:10:00', TIMESTAMP '2026-08-01 11:00:00'),
                                                                                                                 ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                  (SELECT user_id FROM app_users WHERE username = 'mario.rossi'), (SELECT user_id FROM app_users WHERE username = 'giovanni.verdi'),
                                                                                                                  'ACCEPTED', TIMESTAMP '2026-08-01 10:10:00', TIMESTAMP '2026-08-01 12:30:00'),
                                                                                                                 ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                  (SELECT user_id FROM app_users WHERE username = 'mario.rossi'), (SELECT user_id FROM app_users WHERE username = 'anna.russo'),
                                                                                                                  'ACCEPTED', TIMESTAMP '2026-08-01 10:10:00', TIMESTAMP '2026-08-01 09:15:00'),
                                                                                                                 ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                  (SELECT user_id FROM app_users WHERE username = 'mario.rossi'), (SELECT user_id FROM app_users WHERE username = 'paolo.ferrari'),
                                                                                                                  'ACCEPTED', TIMESTAMP '2026-08-01 10:10:00', TIMESTAMP '2026-08-01 14:20:00'),
                                                                                                                 ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                  (SELECT user_id FROM app_users WHERE username = 'mario.rossi'), (SELECT user_id FROM app_users WHERE username = 'sara.gallo'),
                                                                                                                  'PENDING', TIMESTAMP '2026-08-27 09:00:00', NULL);

-- ---------------------------------------------------------------------
-- player (anagrafica dei giocatori reali, come se arrivasse dal
-- simulatore esterno: external_id e' la chiave lato simulatore, price e'
-- la quotazione di mercato corrente, is_injured lo stato infortunio,
-- position il ruolo (P=portiere, D=difensore, C=centrocampista, A=attaccante).
-- Condivisa da tutte le leghe: nessun riferimento a team/lega qui.)
-- ---------------------------------------------------------------------
INSERT INTO player (external_id, name, surname, real_team_name, real_team_shirt_num, price, is_injured, position) VALUES
(1001, 'Alessandro', 'Ferri',      'Juventus',   1,  22, false, 'P'),
(1002, 'Davide',     'Conti',      'Inter',      4,  35, false, 'D'),
(1003, 'Matteo',     'Galli',      'Milan',      8,  28, true,  'C'),
(1004, 'Simone',     'Riva',       'Napoli',     9,  45, false, 'A'),
(1005, 'Federico',   'Moretti',    'Roma',       1,  20, false, 'P'),
(1006, 'Lorenzo',    'Bruno',      'Atalanta',   5,  30, false, 'D'),
(1007, 'Nicolo',     'Fontana',    'Fiorentina', 10, 38, false, 'C'),
(1008, 'Andrea',     'Marino',     'Lazio',      1,  19, false, 'P'),
(1009, 'Stefano',    'Greco',      'Torino',     3,  24, false, 'D'),
(1010, 'Riccardo',   'Barbieri',   'Bologna',    7,  33, false, 'C'),
(1011, 'Marco',      'Villa',      'Juventus',   22, 21, false, 'P'),
(1012, 'Luca',       'Rinaldi',    'Inter',      13, 27, false, 'D'),
(1013, 'Gabriele',   'Costa',      'Milan',      11, 42, false, 'A'),
(1014, 'Emanuele',   'Longo',      'Napoli',     1,  18, false, 'P'),
(1015, 'Tommaso',    'Gatti',      'Roma',       6,  26, false, 'D'),
(1016, 'Filippo',    'De Angelis', 'Atalanta',   17, 31, false, 'C');

-- ---------------------------------------------------------------------
-- team_player (rosa di ogni squadra: possesso di un player in una lega.
-- league_id e' denormalizzato da team_id apposta, per poter garantire con
-- un unique index parziale che un player abbia al piu' un possesso attivo
-- per lega, restando comunque libero di essere posseduto in leghe diverse.)
-- ---------------------------------------------------------------------
INSERT INTO team_player (team_id, league_id, player_id, purchase_date, transfer_date, purchase_price) VALUES
-- I Bomber
((SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Alessandro' AND surname = 'Ferri'), DATE '2026-07-01', NULL, 20),
((SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Davide' AND surname = 'Conti'), DATE '2026-07-01', NULL, 32),
((SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Matteo' AND surname = 'Galli'), DATE '2026-07-01', NULL, 25),
((SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Simone' AND surname = 'Riva'), DATE '2026-07-01', NULL, 40),
-- Real Fanta
((SELECT id FROM team WHERE name = 'Real Fanta'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Federico' AND surname = 'Moretti'), DATE '2026-07-01', NULL, 18),
((SELECT id FROM team WHERE name = 'Real Fanta'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Lorenzo' AND surname = 'Bruno'), DATE '2026-07-01', NULL, 27),
((SELECT id FROM team WHERE name = 'Real Fanta'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Nicolo' AND surname = 'Fontana'), DATE '2026-07-01', NULL, 34),
-- Gli Invincibili
((SELECT id FROM team WHERE name = 'Gli Invincibili'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Andrea' AND surname = 'Marino'), DATE '2026-07-01', NULL, 17),
((SELECT id FROM team WHERE name = 'Gli Invincibili'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Stefano' AND surname = 'Greco'), DATE '2026-07-01', NULL, 21),
((SELECT id FROM team WHERE name = 'Gli Invincibili'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Riccardo' AND surname = 'Barbieri'), DATE '2026-07-01', NULL, 29),
-- FC Imbattibili
((SELECT id FROM team WHERE name = 'FC Imbattibili'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Marco' AND surname = 'Villa'), DATE '2026-07-01', NULL, 19),
((SELECT id FROM team WHERE name = 'FC Imbattibili'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Luca' AND surname = 'Rinaldi'), DATE '2026-07-01', NULL, 24),
((SELECT id FROM team WHERE name = 'FC Imbattibili'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Gabriele' AND surname = 'Costa'), DATE '2026-07-01', NULL, 38),
-- Atletico Fantacalcio
((SELECT id FROM team WHERE name = 'Atletico Fantacalcio'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Emanuele' AND surname = 'Longo'), DATE '2026-07-01', NULL, 16),
((SELECT id FROM team WHERE name = 'Atletico Fantacalcio'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Tommaso' AND surname = 'Gatti'), DATE '2026-07-01', NULL, 23),
((SELECT id FROM team WHERE name = 'Atletico Fantacalcio'), (SELECT id FROM league WHERE invite_code = 'LEGA2026'),
 (SELECT id FROM player WHERE name = 'Filippo' AND surname = 'De Angelis'), DATE '2026-07-01', NULL, 28);

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
-- matchday (giornate REALI di campionato, condivise da tutte le leghe)
-- Numerate 3 e 4 apposta: dimostra che la giornata di una lega (round_number,
-- che riparte da 1 alla nascita della lega) e' indipendente dalla giornata
-- reale di campionato (matchday.number) da cui arrivano i player_results.
-- ---------------------------------------------------------------------
INSERT INTO matchday (number, date, is_closed) VALUES
                                                   (3, DATE '2026-08-23', true),
                                                   (4, DATE '2026-08-30', false);

-- ---------------------------------------------------------------------
-- player_results: dati "arrivati dal servizio esterno" per la giornata reale
-- gia' chiusa (matchday.number = 3), agganciati a player_id. Un risultato e'
-- unico per (player_id, matchday_id) nell'intero sistema: e' cosi' che si
-- simula l'ingestione di risultati esterni, condivisi da tutte le leghe che
-- posseggono quel giocatore, non duplicati per lega.
-- clean_sheet e' lasciato NULL per centrocampisti/attaccanti: e' una statistica
-- che ha senso solo per portieri/difensori.
-- ---------------------------------------------------------------------
INSERT INTO player_results (matchday_id, player_id, rating, goal_num, goal_conceded, autogoal_num, assist_num, penalty_saved, penalty_failed, clean_sheet, yellow_card, red_card) VALUES
-- Portieri
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Alessandro' AND surname = 'Ferri'),      6.5, 0, 1, 0, 0, 1, 0, false, 0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Federico'   AND surname = 'Moretti'),    6.0, 0, 2, 0, 0, 0, 0, false, 0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Andrea'     AND surname = 'Marino'),     7.0, 0, 0, 0, 0, 0, 0, true,  0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Emanuele'   AND surname = 'Longo'),      6.0, 0, 1, 0, 0, 0, 0, false, 1, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Marco'      AND surname = 'Villa'),      6.0, 0, 0, 0, 0, 0, 0, false, 0, false),
-- Difensori
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Davide'     AND surname = 'Conti'),      6.5, 0, 0, 0, 1, 0, 0, false, 0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Lorenzo'    AND surname = 'Bruno'),      6.0, 1, 0, 0, 0, 0, 0, false, 1, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Stefano'    AND surname = 'Greco'),      5.5, 0, 0, 1, 0, 0, 0, true,  0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Luca'       AND surname = 'Rinaldi'),    6.5, 0, 0, 0, 1, 0, 0, false, 0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Tommaso'    AND surname = 'Gatti'),      5.5, 0, 0, 0, 0, 0, 0, false, 1, true),
-- Centrocampisti
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Matteo'     AND surname = 'Galli'),      5.0, 0, 0, 0, 0, 0, 0, NULL,  0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Nicolo'     AND surname = 'Fontana'),    7.0, 1, 0, 0, 1, 0, 0, NULL,  0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Riccardo'   AND surname = 'Barbieri'),   6.5, 0, 0, 0, 1, 0, 0, NULL,  0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Filippo'    AND surname = 'De Angelis'), 6.0, 0, 0, 0, 0, 0, 1, NULL,  1, false),
-- Attaccanti
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Simone'     AND surname = 'Riva'),       8.0, 2, 0, 0, 0, 0, 0, NULL,  0, false),
((SELECT id FROM matchday WHERE number = 3), (SELECT id FROM player WHERE name = 'Gabriele'   AND surname = 'Costa'),      7.0, 1, 0, 0, 0, 0, 0, NULL,  0, false);

-- ---------------------------------------------------------------------
-- league_match
-- Giornata di lega 1 = giornata reale 3 (23/08): gia' giocata, con punteggi.
--   Atletico Fantacalcio riposa.
-- Giornata di lega 2 = giornata reale 4 (30/08): non ancora giocata (punteggi NULL).
--   I Bomber riposa.
-- ---------------------------------------------------------------------
INSERT INTO league_match (league_id, home_team_id, away_team_id, home_score, away_score, home_goals, away_goals, match_day, matchday_id, round_number) VALUES
                                                                                                                                                           ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                                                            (SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM team WHERE name = 'Real Fanta'),
                                                                                                                                                            68.5, 54.0, 2, 1, TIMESTAMP '2026-08-23 15:00:00', (SELECT id FROM matchday WHERE number = 3), 1),
                                                                                                                                                           ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                                                            (SELECT id FROM team WHERE name = 'Gli Invincibili'), (SELECT id FROM team WHERE name = 'FC Imbattibili'),
                                                                                                                                                            45.0, 72.5, 0, 3, TIMESTAMP '2026-08-23 15:00:00', (SELECT id FROM matchday WHERE number = 3), 1),
                                                                                                                                                           ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                                                            (SELECT id FROM team WHERE name = 'Real Fanta'), (SELECT id FROM team WHERE name = 'Gli Invincibili'),
                                                                                                                                                            NULL, NULL, NULL, NULL, TIMESTAMP '2026-08-30 15:00:00', (SELECT id FROM matchday WHERE number = 4), 2),
                                                                                                                                                           ((SELECT id FROM league WHERE invite_code = 'LEGA2026'),
                                                                                                                                                            (SELECT id FROM team WHERE name = 'FC Imbattibili'), (SELECT id FROM team WHERE name = 'Atletico Fantacalcio'),
                                                                                                                                                            NULL, NULL, NULL, NULL, TIMESTAMP '2026-08-30 15:00:00', (SELECT id FROM matchday WHERE number = 4), 2);

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
-- player_id e' una vera foreign key verso team_player(id): ogni riga
-- schiera un giocatore realmente posseduto dalla squadra in quella lineup.
-- La rosa di test e' volutamente piccola (3-4 giocatori a squadra), quindi
-- qui schieriamo tutti i team_player disponibili invece di un 11 completo.
-- ---------------------------------------------------------------------
-- I Bomber: tutta la rosa disponibile (4 giocatori)
INSERT INTO lineup_player (lineup_id, player_id, starter) VALUES
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND player_id = (SELECT id FROM player WHERE name = 'Alessandro' AND surname = 'Ferri')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND player_id = (SELECT id FROM player WHERE name = 'Davide' AND surname = 'Conti')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND player_id = (SELECT id FROM player WHERE name = 'Matteo' AND surname = 'Galli')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND player_id = (SELECT id FROM player WHERE name = 'Simone' AND surname = 'Riva')), true);

-- Real Fanta: tutta la rosa disponibile (3 giocatori)
INSERT INTO lineup_player (lineup_id, player_id, starter) VALUES
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta') AND player_id = (SELECT id FROM player WHERE name = 'Federico' AND surname = 'Moretti')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta') AND player_id = (SELECT id FROM player WHERE name = 'Lorenzo' AND surname = 'Bruno')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta') AND player_id = (SELECT id FROM player WHERE name = 'Nicolo' AND surname = 'Fontana')), true);

-- Gli Invincibili: tutta la rosa disponibile (3 giocatori)
INSERT INTO lineup_player (lineup_id, player_id, starter) VALUES
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili') AND player_id = (SELECT id FROM player WHERE name = 'Andrea' AND surname = 'Marino')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili') AND player_id = (SELECT id FROM player WHERE name = 'Stefano' AND surname = 'Greco')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili') AND player_id = (SELECT id FROM player WHERE name = 'Riccardo' AND surname = 'Barbieri')), true);

-- FC Imbattibili: 2 titolari + 1 riserva (3 giocatori disponibili)
INSERT INTO lineup_player (lineup_id, player_id, starter) VALUES
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili') AND player_id = (SELECT id FROM player WHERE name = 'Marco' AND surname = 'Villa')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili') AND player_id = (SELECT id FROM player WHERE name = 'Gabriele' AND surname = 'Costa')), true),
                                                                        ((SELECT id FROM lineup WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili')),
                                                                         (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili') AND player_id = (SELECT id FROM player WHERE name = 'Luca' AND surname = 'Rinaldi')), false);

-- ---------------------------------------------------------------------
-- trade: una per stato (PENDING / ACCEPTED / REJECTED), con importi che
-- coprono i tre casi: offerta di denaro extra, richiesta di denaro, nessun denaro.
-- ---------------------------------------------------------------------
INSERT INTO trade (proposing_team_id, receiving_team_id, trade_player_id, offered_player_id, amount, status, proposal_date) VALUES
-- I Bomber offre Galli + 20 crediti per avere Fontana da Real Fanta (PENDING)
((SELECT id FROM team WHERE name = 'I Bomber'), (SELECT id FROM team WHERE name = 'Real Fanta'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Real Fanta') AND player_id = (SELECT id FROM player WHERE name = 'Nicolo' AND surname = 'Fontana')),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'I Bomber') AND player_id = (SELECT id FROM player WHERE name = 'Matteo' AND surname = 'Galli')),
 20, 'PENDING', TIMESTAMP '2026-08-24 09:00:00'),

-- Gli Invincibili offre Barbieri e chiede 15 crediti indietro per avere Costa da FC Imbattibili (ACCEPTED)
((SELECT id FROM team WHERE name = 'Gli Invincibili'), (SELECT id FROM team WHERE name = 'FC Imbattibili'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili') AND player_id = (SELECT id FROM player WHERE name = 'Gabriele' AND surname = 'Costa')),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Gli Invincibili') AND player_id = (SELECT id FROM player WHERE name = 'Riccardo' AND surname = 'Barbieri')),
 -15, 'ACCEPTED', TIMESTAMP '2026-08-22 18:30:00'),

-- FC Imbattibili offre Rinaldi (nessun denaro) per avere De Angelis da Atletico Fantacalcio (REJECTED)
((SELECT id FROM team WHERE name = 'FC Imbattibili'), (SELECT id FROM team WHERE name = 'Atletico Fantacalcio'),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'Atletico Fantacalcio') AND player_id = (SELECT id FROM player WHERE name = 'Filippo' AND surname = 'De Angelis')),
 (SELECT id FROM team_player WHERE team_id = (SELECT id FROM team WHERE name = 'FC Imbattibili') AND player_id = (SELECT id FROM player WHERE name = 'Luca' AND surname = 'Rinaldi')),
 NULL, 'REJECTED', TIMESTAMP '2026-08-25 12:00:00');

COMMIT;
