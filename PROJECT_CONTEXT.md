# LeagueSim — contesto per integrazione con il progetto Fantafootball

> Questo file è pensato per essere letto da un'altra sessione/agente che lavora
> sul progetto "fantafootball" (fantacalcio), per capire cosa espone LeagueSim
> e come collegarsi correttamente. Percorso di questo repo:
> `C:\Dev\Java\Projects\LeagueSim`.

## Cos'è LeagueSim

Microservizio Spring Boot (Java 26, Spring Boot 4.1.1) che simula un
campionato di calcio "sintetico": genera calendario, squadre, giocatori, e
simula i risultati di ogni giornata con statistiche per giocatore (voto, gol,
assist, cartellini, ecc.). **Non ha UI**: è un backend puro, pensato per
essere consumato da un altro servizio — il progetto fantacalcio — che userà
questi dati (voti, gol, assist...) per calcolare i punteggi fantacalcio.

Questo è dichiarato esplicitamente nel codice: vedi il commento in
`ApiKeyAuthFilter.java` — "There is no end-user login here: the caller is
another Spring Boot service (the fantacalcio project) reading data."

## Stack tecnico

- Java 26, Spring Boot 4.1.1 (starter parent)
- Spring Data JPA + PostgreSQL (driver `org.postgresql`)
- Flyway per le migrazioni (`src/main/resources/db/migration`)
- Spring Security, ma **stateless** e basata su API key statica (nessun login utente)
- Nessun frontend/template engine: solo REST controller (`spring-boot-starter-webmvc`)

## Come si autentica un client esterno (es. fantafootball)

- Header richiesto su **tutte** le rotte `/api/**`: `X-API-KEY: <valore>`
- Il valore atteso è configurato da `leaguesim.security.api-key`
  (env var `LEAGUESIM_API_KEY`, default locale `local-dev-api-key`)
- Confronto fatto con `MessageDigest.isEqual` (timing-safe) in
  `ApiKeyAuthFilter`
- Se la chiave manca/non combacia, la richiesta non viene autenticata e
  `SecurityConfig` nega l'accesso (`anyRequest().denyAll()` fuori da `/api/**`,
  `authenticated()` su `/api/**`)
- Non c'è alcuna nozione di utente/ruolo differenziato: il chiamante autenticato
  ottiene sempre `ROLE_SERVICE` con principal fisso `"fantacalcio-service"`

Per collegare il progetto fantafootball, questo servizio deve girare come
processo separato raggiungibile via HTTP, con la stessa API key configurata
su entrambi i lati (env var condivisa o secret condiviso).

## Modello di dominio

- **Team**: nome (univoco) + `strength` (1-100), usata per pesare la
  simulazione dei match
- **Player**: nome/cognome, squadra (FK), numero maglia, `position`
  (`P`=portiere, `D`=difensore, `C`=centrocampista, `A`=attaccante), prezzo
  (usato come proxy di qualità/valore), flag `injured`
- **Matchday** (giornata): numero (univoco), data, flag `closed`
- **Match**: giornata + squadra casa/trasferta, gol casa/trasferta (nullable
  finché non giocato), flag `played`
- **PlayerMatchStats**: statistiche per giocatore per partita — `rating`
  (BigDecimal 4,2), gol, gol subiti, autogol, assist, rigori parati/falliti,
  clean sheet, gialli, rosso, `starter`. Vincolo univoco (player_id, match_id)

Schema SQL completo in `V1__init_schema.sql`; dati demo (4 squadre, 18
giocatori ciascuna) in `V2__seed_data.sql`.

## Logica di simulazione

- `FixtureGenerationService`: genera un girone di andata/ritorno completo
  (metodo del cerchio) per tutte le squadre presenti, **solo se il DB è vuoto**
  (`generateSeasonIfEmpty`, invocato allo startup da `StartupFixtureRunner`).
  Prima giornata fissata a "oggi + 1 settimana".
- `MatchSimulationService`: simula una singola partita in modo probabilistico
  (Poisson per il numero di occasioni da gol, pesato sulla `strength` relativa
  delle squadre; home advantage incorporato). Assegna gol/assist/autogol/rigori
  /cartellini/voto per ogni titolare, selezionando gli 11 titolari per ruolo
  (1 P / 4 D / 3 C / 3 A, con top-up per prezzo se mancano giocatori
  disponibili) escludendo gli infortunati.
- `MatchdaySimulationService`: orchestratore transazionale — simula tutte le
  partite non ancora giocate di una giornata, salva risultati e statistiche,
  poi marca la giornata come `closed`.

## API esposte (tutte sotto `/api`, richiedono `X-API-KEY`)

| Metodo | Path | Descrizione |
|---|---|---|
| GET | `/api/players` | Lista tutti i giocatori (con nome squadra) |
| GET | `/api/players/{id}` | Dettaglio giocatore |
| GET | `/api/matchdays` | Lista giornate (numero, data, closed) |
| GET | `/api/matchdays/{number}/results` | Statistiche per giocatore di quella giornata (voto, gol, assist, ecc.) |
| POST | `/api/admin/matchdays/{number}/simulate` | Simula (se non già giocata) tutte le partite della giornata e la chiude |

DTO di risposta: `PlayerDto`, `MatchdayDto`, `PlayerMatchResultDto` (vedi
`web/dto/`). Nota: `PlayerMatchResultDto` espone solo `playerId`, non i dati
anagrafici — per associare le stats al giocatore/squadra, il consumer deve
incrociare con `/api/players`.

## Configurazione (`application.properties`)

```
spring.datasource.url=${LEAGUESIM_DB_URL:jdbc:postgresql://localhost:5432/leaguesim}
spring.datasource.username=${DB_USER:postgresMaster}
spring.datasource.password=${DB_PASS:goPostgresGo}
leaguesim.security.api-key=${LEAGUESIM_API_KEY:local-dev-api-key}
```

Per integrare fantafootball in locale: puntare a un DB Postgres proprio
(`leaguesim`), avviare LeagueSim (porta di default Spring Boot, 8080, non
sovrascritta nel file), e configurare lo stesso `LEAGUESIM_API_KEY` sul
servizio fantafootball come header `X-API-KEY` nelle chiamate HTTP verso
questo servizio.

## Cosa NON c'è (attenzione per chi integra)

- Nessun endpoint di scrittura per creare/modificare team, player o injuries
  (i dati arrivano solo dal seed Flyway V2)
- Nessuna gestione di più stagioni/campionati: `generateSeasonIfEmpty` gira
  una sola volta e basta finché il DB non è vuoto
- Nessuna paginazione sulle liste (`/api/players`, `/api/matchdays`)
- Nessun CORS configurato esplicitamente — se fantafootball ha un frontend
  browser che chiama direttamente LeagueSim, andrà aggiunta una config CORS
- Nessuna gestione di ruoli/utenti finali, solo autenticazione service-to-service

## Punti da chiarire quando si progetta l'integrazione

- Come/quando fantafootball invoca `POST /api/admin/matchdays/{n}/simulate`
  (schedulazione autonoma di LeagueSim vs. trigger esterno da fantafootball)
- Se fantafootball ha bisogno di un mapping stabile tra `Player.id` di
  LeagueSim e le proprie entità (es. "calciatore fantacalcio") — al momento
  l'unico identificatore condiviso è `Player.id`/`Team.name`
- Se serve un endpoint aggregato (es. classifica, storico) non ancora presente
