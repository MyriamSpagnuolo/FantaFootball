-- =====================================================================
-- Seed per testare manualmente il flusso di asta (import/acquisto player
-- in una squadra). Crea utenti, lega e squadre vuote (nessun team_player):
-- e' pensato per essere eseguito prima di chiamare a mano gli endpoint di
-- asta, che poi popoleranno team_player/budget.
--
-- Riavviabile: svuota app_users, league e tutte le tabelle che dipendono
-- da esse (CASCADE), inclusi eventuali team_player/league_match/lineup/
-- trade/league_invite creati durante un giro di test precedente, e resetta
-- le sequenze coinvolte. La tabella player (e matchday/lineup_type) NON
-- viene toccata: e' il catalogo condiviso, sincronizzato da LeagueSim.
--
-- Password in chiaro per TUTTI gli utenti di test: Fantacalcio1!
-- (rispetta i vincoli di StrongPasswordValidator: >=12 caratteri,
-- maiuscola, minuscola, cifra, carattere speciale)
-- =====================================================================

BEGIN;

TRUNCATE TABLE
    app_user_roles,
    password_reset_token,
    app_users,
    league,
    league_invite,
    league_match,
    lineup,
    lineup_player,
    team,
    team_player,
    trade
    RESTART IDENTITY CASCADE;

ALTER SEQUENCE seq_app_users_user_id RESTART WITH 1;
ALTER SEQUENCE seq_league_id RESTART WITH 1;
ALTER SEQUENCE seq_league_invite_id RESTART WITH 1;
ALTER SEQUENCE seq_league_match_id RESTART WITH 1;
ALTER SEQUENCE seq_lineup_id RESTART WITH 1;
ALTER SEQUENCE seq_team_id RESTART WITH 1;
ALTER SEQUENCE seq_team_player_id RESTART WITH 1;
ALTER SEQUENCE seq_trade_id RESTART WITH 1;


-- ============================================================
-- UTENTI
-- ============================================================

INSERT INTO app_users
(username, email, password_hash, enabled, token_version)
VALUES
    (
        'Matteo De Cata',
        'matteo.decata@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    ),
    (
        'Matthew of Cat',
        'matthew.ofcat@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    ),
    (
        'El Presi',
        'el.presi@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    ),
    (
        'Pres',
        'pres@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    ),
    (
        'Mr Andreotti',
        'mr.andreotti@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    ),
    (
        'MDC',
        'mdc@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    ),
    (
        'King of HufflePuff',
        'king.ofhufflepuff@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    ),
    (
        'PatrizioOfficial',
        'patrizioofficial@example.com',
        '$2a$10$7hEWiFbv4hwZvvsxrO10c.634gabgrNJTb4cjdrb4vvz4XZulHQji',
        TRUE,
        0
    );


-- ============================================================
-- RUOLI
-- ============================================================
-- Gli ID vengono recuperati tramite username,
-- quindi non assumiamo che Matteo abbia necessariamente ID 1.
-- ============================================================

INSERT INTO app_user_roles (user_id, role)
SELECT user_id, 'ADMIN'
FROM app_users
WHERE username = 'Matteo De Cata';

INSERT INTO app_user_roles (user_id, role)
SELECT user_id, 'USER'
FROM app_users
WHERE username IN (
                   'Matteo De Cata',
                   'Matthew of Cat',
                   'El Presi',
                   'Pres',
                   'Mr Andreotti',
                   'MDC',
                   'King of HufflePuff',
                   'PatrizioOfficial'
    );


-- ============================================================
-- LEGA
-- ============================================================

INSERT INTO league
(name, invite_code, admin_user_id, creation_date, budget)
SELECT
    'Marius Rodgers League, No Robber',
    'MARIUS2026',
    user_id,
    '2026-09-09 10:00:00',
    500
FROM app_users
WHERE username = 'Matteo De Cata';


-- ============================================================
-- SQUADRE
-- ============================================================
-- Anche qui recuperiamo user_id e league_id tramite query.
-- ============================================================

INSERT INTO team
(name, user_id, league_id, budget, total_points)
SELECT
    'Paguri FC',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'Matteo De Cata'
  AND l.name = 'Marius Rodgers League, No Robber'

UNION ALL

SELECT
    'AC Frochok',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'Matthew of Cat'
  AND l.name = 'Marius Rodgers League, No Robber'

UNION ALL

SELECT
    'FirstTimeEverInSardinia',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'El Presi'
  AND l.name = 'Marius Rodgers League, No Robber'

UNION ALL

SELECT
    '120kg massa magra',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'Pres'
  AND l.name = 'Marius Rodgers League, No Robber'

UNION ALL

SELECT
    'PistacchioMaNonQuelPistacchio',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'Mr Andreotti'
  AND l.name = 'Marius Rodgers League, No Robber'

UNION ALL

SELECT
    'AllGodsButDifferentCategories',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'MDC'
  AND l.name = 'Marius Rodgers League, No Robber'

UNION ALL

SELECT
    'Sharon Sdikcs',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'King of HufflePuff'
  AND l.name = 'Marius Rodgers League, No Robber'

UNION ALL

SELECT
    'Matthew Oxford Money',
    u.user_id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'PatrizioOfficial'
  AND l.name = 'Marius Rodgers League, No Robber';


COMMIT;