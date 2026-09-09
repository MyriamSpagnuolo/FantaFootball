-- =====================================================================
-- Seed dei moduli standard di formazione (lineup_type).
--
-- Tabella di riferimento fissa, senza generazione automatica dell'id
-- (vedi commento in LineupType.java): nessun endpoint la gestisce.
-- Dalla introduzione di LineupTypeSeeder (CommandLineRunner) l'app la
-- popola gia' da sola all'avvio (stessi id, tramite Formation enum);
-- questo script resta utile solo per un DB non ancora avviato dall'app
-- (es. accesso diretto/altri strumenti). Riavviabile: ON CONFLICT (id)
-- DO NOTHING, quindi non tocca righe gia' presenti ne' richiede un
-- TRUNCATE preventivo.
--
-- Stessi 7 moduli gia' presenti in seed-data.sql (che pero' tronca anche
-- tutte le altre tabelle) — usare questo file per seedare solo lineup_type
-- senza toccare il resto del DB.
-- =====================================================================

INSERT INTO lineup_type (id, defender_num, midfielder_num, foward_num) VALUES
    (1, 3, 4, 3), -- 3-4-3
    (2, 3, 5, 2), -- 3-5-2
    (3, 4, 3, 3), -- 4-3-3
    (4, 4, 4, 2), -- 4-4-2
    (5, 4, 5, 1), -- 4-5-1
    (6, 5, 3, 2), -- 5-3-2
    (7, 5, 4, 1)  -- 5-4-1
ON CONFLICT (id) DO NOTHING;
