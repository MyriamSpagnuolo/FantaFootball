BEGIN;

-- ============================================================
-- RESET DATI UTENTI / LEGA / SQUADRE
-- ============================================================
-- CASCADE elimina automaticamente tutti i dati che dipendono
-- da app_users: team, league, league_match, lineup, trade,
-- league_invite, team_player, ecc.
--
-- La tabella player NON viene toccata.
-- ============================================================

TRUNCATE TABLE app_users RESTART IDENTITY CASCADE;


-- ============================================================
-- UTENTI
-- ============================================================
-- Password di test per tutti:
-- Fantacalcio1!
--
-- Tutti gli utenti hanno ruolo USER.
-- Matteo De Cata è anche ADMIN della lega.
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

INSERT INTO app_user_roles (user_id, role)
VALUES
    (1, 'ADMIN'),
    (1, 'USER'),
    (2, 'USER'),
    (3, 'USER'),
    (4, 'USER'),
    (5, 'USER'),
    (6, 'USER'),
    (7, 'USER'),
    (8, 'USER');


-- ============================================================
-- LEGA
-- ============================================================

INSERT INTO league
(name, invite_code, admin_user_id, creation_date, budget)
VALUES
    (
        'Marius Rodgers League, No Robber',
        'MARIUS2026',
        1,
        '2026-09-09 10:00:00',
        500
    );


-- ============================================================
-- SQUADRE
-- ============================================================
-- Tutte le squadre appartengono alla stessa lega.
-- Budget iniziale: 500
-- Total points: 0
-- Nessun giocatore acquistato.
-- ============================================================

INSERT INTO team
(name, user_id, league_id, budget, total_points)
VALUES
    ('Paguri FC', 1, 1, 500, 0),
    ('AC Frochok', 2, 1, 500, 0),
    ('FirstTimeEverInSardinia', 3, 1, 500, 0),
    ('120kg massa magra', 4, 1, 500, 0),
    ('PistacchioMaNonQuelPistacchio', 5, 1, 500, 0),
    ('AllGodsButDifferentCategories', 6, 1, 500, 0),
    ('Sharon Sdikcs', 7, 1, 500, 0),
    ('Matthew Oxford Money', 8, 1, 500, 0);


COMMIT;