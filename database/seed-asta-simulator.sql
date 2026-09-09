BEGIN;

-- ============================================================
-- RESET DATI UTENTI / LEGA / SQUADRE
-- ============================================================
-- Svuota app_users e tutte le tabelle che dipendono da essa.
-- La tabella player NON viene toccata.
-- ============================================================

TRUNCATE TABLE app_users RESTART IDENTITY CASCADE;


-- ============================================================
-- UTENTI
-- ============================================================

INSERT INTO app_users
(username, email, password_hash, enabled, token_version)
VALUES
    (
        'Matteo De Cata',
        'matteo.decata@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        TRUE,
        0
    ),
    (
        'Matthew of Cat',
        'matthew.ofcat@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        TRUE,
        0
    ),
    (
        'El Presi',
        'el.presi@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        TRUE,
        0
    ),
    (
        'Pres',
        'pres@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        TRUE,
        0
    ),
    (
        'Mr Andreotti',
        'mr.andreotti@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        TRUE,
        0
    ),
    (
        'MDC',
        'mdc@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        TRUE,
        0
    ),
    (
        'King of HufflePuff',
        'king.ofhufflepuff@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        TRUE,
        0
    ),
    (
        'PatrizioOfficial',
        'patrizioofficial@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
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
SELECT id, 'ADMIN'
FROM app_users
WHERE username = 'Matteo De Cata';

INSERT INTO app_user_roles (user_id, role)
SELECT id, 'USER'
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
    id,
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
    u.id,
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
    u.id,
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
    u.id,
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
    u.id,
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
    u.id,
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
    u.id,
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
    u.id,
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
    u.id,
    l.id,
    500,
    0
FROM app_users u
         CROSS JOIN league l
WHERE u.username = 'PatrizioOfficial'
  AND l.name = 'Marius Rodgers League, No Robber';


COMMIT;