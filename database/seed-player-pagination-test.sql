-- PostgreSQL: eseguire nel database fantafootball da pgAdmin / DBeaver.
-- Aggiunge 100 giocatori: 25 per ruolo, con ID in ordine A/C/D/P
-- e cognomi in ordine inverso, per rendere evidente un ordinamento errato.
-- Non assegna giocatori alle rose e non modifica i dati esistenti.
-- Riavviabile: external_id negativi riservati a questo seed, senza duplicati.
--
-- Richieste autenticate (page parte da 0):
-- GET /api/players?search=PaginationSeed&size=20&page=0
-- GET /api/leagues/{leagueId}/players/available?search=PaginationSeed&size=20&page=0
-- Per il secondo endpoint usare una lega a cui si ha accesso.
-- Ripetere con page=1,2,3,4,5. Non acquistare questi giocatori durante la prova.
-- Il filtro search isola il seed anche quando esistono altri giocatori.
--
-- Atteso (cognomi crescenti all'interno del ruolo):
-- page=0: 20 P              (P 01-20)
-- page=1:  5 P + 15 D       (P 21-25, D 01-15)
-- page=2: 10 D + 10 C       (D 16-25, C 01-10)
-- page=3: 15 C +  5 A       (C 11-25, A 01-05)
-- page=4: 20 A              (A 06-25)
-- page=5: content vuoto
-- Sempre totalElements=100 e totalPages=5; hasNext=true solo per page 0-3.

BEGIN;

INSERT INTO public.player (
    id, external_id, name, surname, real_team_name,
    real_team_shirt_num, price, is_injured, position
)
SELECT nextval('public.seq_player_id'),
       -910000000 - ((n - 1) * 4 + r.slot),
       'PaginationSeed',
       'Giocatore ' || lpad(n::text, 2, '0'),
       'Pagination Test FC',
       n,
       10 + n,
       false,
       r.role
FROM generate_series(1, 25) AS series(n)
CROSS JOIN (VALUES (1, 'A'), (2, 'C'), (3, 'D'), (4, 'P')) AS r(slot, role)
ORDER BY n DESC, r.slot
ON CONFLICT (external_id) DO NOTHING;

COMMIT;

-- Riepilogo dei soli giocatori del seed: attese quattro righe da 25.
SELECT position, count(*) AS players
FROM public.player
WHERE external_id BETWEEN -910000100 AND -910000001
  AND name = 'PaginationSeed'
GROUP BY position
ORDER BY CASE position WHEN 'P' THEN 0 WHEN 'D' THEN 1 WHEN 'C' THEN 2 ELSE 3 END;
