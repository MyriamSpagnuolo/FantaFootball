# FantaFootball — guida per agenti AI

> Aggiornato il 12 settembre 2026. Leggere prima [CONTESTO_COMPLETO.md](CONTESTO_COMPLETO.md), il riferimento principale per stato del progetto, API, dominio, flussi, configurazione e limiti verificati. Questa guida conserva le convenzioni operative; non sostituisce il controllo del codice.

## Stack
Java 26, Spring Boot 4.1.0, Spring Data JPA + PostgreSQL/Hikari, Spring Security (JWT stateless via oauth2-resource-server), Spring RestClient, springdoc-openapi.

## Moduli principali
- `controllers/` — REST, estrazione input (JWT claims, `@Valid` DTO) e delega al service; esistono anche il controllo admin del calendario e la transazione di creazione lega con squadra.
- `services/` — logica di business **e autorizzazione manuale** (ownership/admin check), niente `@PreAuthorize`.
- `model/entities|dto|repositories|exceptions|specifications|validation` — dominio, DTO record con `fromEntity(...)`, eccezioni custom.
- `security/` — JWT stateless, filtro di validità account, bootstrap utente HEAD.
- `integration/leaguesim/` — client HTTP verso il microservizio esterno LeagueSim (voti/gol/prezzi giocatori); vedi il contesto completo per il contratto consumato e `PROJECT_CONTEXT.md` per le note storiche non riverificate sul servizio esterno.
- `calculateMatchday/` — motore di calcolo del punteggio fantacalcistico di una formazione.
- `algorithms/` — generazione calendario (round robin).

## Pattern architetturali da rispettare
- Il pattern prevalente estrae `userId` dal claim JWT `uid` e lo passa al service per i controlli di autorizzazione (nessun `@PreAuthorize`, pur essendo `@EnableMethodSecurity` attivo). Non assumere che tutte le rotte seguano lo stesso schema: il POST calendario controlla l'admin nel controller, alcune letture richiedono solo autenticazione.
- Integrazione LeagueSim a 3 livelli: `LeagueSimClient` (solo HTTP, nessuna logica) → `LeagueSimSyncService` (decide cosa/quando) → `LeagueSimMatchdayImportService` (bean **separato** apposta: se fosse un metodo richiamato internamente da `SyncService`, il proxy `@Transactional` di Spring verrebbe bypassato per self-invocation).
- `LeagueSimSyncService.syncMatchdays()` fa l'upsert di **ogni** giornata restituita da LeagueSim (`/api/matchdays`), aperta o chiusa: solo per quelle chiuse prosegue con l'import dei risultati. È il motivo per cui `LeagueMatchService.generateCalendar()` (che pesca `matchdayRepository.findByClosedFalseOrderByDateAsc()`) ha giornate `is_closed=false` con cui lavorare in un ambiente reale — se questo metodo scartasse le giornate aperte (come faceva la vecchia versione `syncResults`), generateCalendar non avrebbe mai dati e fallirebbe con `invalid_rounds`.
- L'upsert dei giocatori LeagueSim avviene per `externalId` (mai per nome/cognome — vedi "Da non fare"); le giornate sono identificate per `number`, i risultati per la coppia player locale/giornata locale.
- Gli `@Scheduled(fixedDelayString=...)` senza `initialDelay` esplicito partono **subito all'avvio**, poi rispettano l'intervallo dalla fine dell'esecuzione precedente.
- Decisione presa: FantaFootball non avrà mai un trigger (manuale o via endpoint admin) per **far partire** la simulazione delle giornate su LeagueSim — resta interamente automatico via `LeagueSimScheduler`, che richiama `LeagueSimSyncService.syncPlayers()`/`syncMatchdays()` a intervalli fissi (`leaguesim.players-sync-interval`/`results-sync-interval`). FantaFootball resta un consumatore passivo dello stato di LeagueSim, mai un iniziatore.

## Autorizzazioni
- `UserRole` globale (`ADMIN`/`USER`) è usato solo per il bootstrap dell'utente HEAD, **non** per decisioni a livello di lega.
- L'"admin di lega" è un concetto contestuale, non il ruolo globale: è l'`AppUser` referenziato da `League.admin`, verificato ovunque con `userId.equals(league.getAdmin().getId())` (`TeamService`, `LeagueInviteService`, `ExternalAuctionImportService`). Lo stesso utente può essere admin in una lega e membro semplice in un'altra.
- `removePlayerFromTeam`, lettura formazione e voti rosa sono permessi al proprietario o all'admin di lega. `getTeamRoster` è invece accessibile a qualsiasi utente autenticato e restituisce anche possessi trasferiti; non verifica ownership e non filtra gli attivi.
- `EnabledAccountFilter` ri-verifica su DB dopo la validazione JWT: `enabled=true`, `tokenVersion` combaciante e username uguale al subject. Incrementare `AppUser.tokenVersion` (reset password, cambio password o username) invalida tutti i JWT già emessi anche se non scaduti. Il normale logout elimina solo il token lato client.

## Configurazioni critiche
- `spring.jpa.hibernate.ddl-auto=validate`: lo schema **non** è auto-gestito. Ogni modifica a un'entità richiede modifica manuale a `database/ddl.sql` + DB reale, altrimenti l'app non parte (niente Flyway qui, a differenza di LeagueSim).
- `app.jwt.secret` deve avere ≥32 byte, nessun default — fail-fast in `SecurityConfig.jwtSecretKey`.
- `leaguesim.api-key` non ha default — fail-fast in `LeagueSimProperties`.
- `server.port=8081` di default apposta per non collidere con LeagueSim (porta 8080).
- `OpenApiConfig.fantaFootballOpenAPI()` ha l'URL del server Swagger **hardcoded** (`http://localhost:8081`): se cambi `server.port` vanno allineati entrambi, altrimenti "Try it out" nella Swagger UI chiama la porta sbagliata (es. quella di LeagueSim).
- `APP_HEAD_PASSWORD` non ha default e rifiuta espressamente "head" e "password": se assente o uguale a questi valori, la creazione dell'utente HEAD viene saltata con warning. Non applica il validatore completo `@StrongPassword`.

## Convenzioni
- Eccezioni custom (`NotFoundException`, `BadRequestException`, `ConflictException`) portano `errorCode` + `message`, mappate da `GlobalExceptionHandler` in `{errorCode, message}` JSON — è la forma attesa per ogni nuova eccezione di business.
- Idempotenza: gli upsert da sistemi esterni risolvono-o-creano per chiave esterna stabile, mai per campi descrittivi.
- Commenti lunghi in italiano spiegano il *perché* soprattutto in `integration/leaguesim` e `calculateMatchday` — leggerli prima di modificare quel codice, codificano vincoli non ovvi (es. motivo del bean separato, formula del modificatore difesa).

## Da non fare
- Non usare il ruolo globale `ADMIN`/`USER` come unico guard per azioni a livello di lega: verificare sempre `League.admin`.
- Non abbinare `Player` per nome/cognome quando si importano risultati esterni: è fragile a omonimie/refusi — usare invece il pattern per `externalId` di `LeagueSimMatchdayImportService`.
- Non chiamare un metodo `@Transactional` della stessa classe aspettandosi che la transazione si applichi (self-invocation bypassa il proxy Spring): separare in un bean diverso come già fatto per l'import LeagueSim.
- Non assumere che Hibernate crei/alteri le tabelle: `ddl-auto=validate` richiede modifica manuale di `database/ddl.sql`.
- Non lanciare `ResponseStatusException` con stringa ad-hoc `"code: message"` per nuovo codice: rompe la forma strutturata `{errorCode, message}` prodotta da `GlobalExceptionHandler` per le altre eccezioni. Usare `NotFoundException`/`ConflictException`/`BadRequestException`, oppure `AccessDeniedException` di Spring Security per i 403 (non esiste una `ForbiddenException` custom nel progetto).

## Fantavoto del singolo player
- `PlayerMatchStats.calculateFantaRating(PlayerResult)` (static) calcola il fantavoto di un giocatore da un `PlayerResult`, senza bisogno di `LineupPlayer`/lineup — usato sia dal percorso aggregato (`MatchdayCalculationService.calculateLineupScore`, tramite l'overload d'istanza) sia da `MatchdayCalculationService.calculatePlayerRating(playerId, matchdayId)`, esposto da `GET /api/players/{playerId}/matchdays/{matchdayId}/rating`.
- Questo endpoint copre anche i giocatori mai schierati in nessuna lineup (o messi in panchina e mai sostituiti), che `calculateLineupScore` non calcola perché itera solo `lineup.getPlayers()`.

## Contratti da non confondere

- Formazioni: GET/POST/PUT `/api/teams/{teamId}/matches/{leagueMatchId}/lineup` sono già presenti. Scrittura solo del proprietario, fino alla chiusura locale della giornata.
- Calendario: GET e POST `/api/leagues/{leagueId}/matches` sono già presenti. Il polling salva risultati e aggiorna la classifica.
- Catalogo e disponibili restituiscono `PageResponse`, ordinata per P/D/C/A, cognome, nome e ID, non per solo ID.
- Acquisto e fantavoto individuale usano `Player.id`; scambi, formazione e svincolo usano `TeamPlayer.id`, anche dove il parametro si chiama `playerId`.
- La classifica REST ordina per punti: il metodo di `Standing` con spareggio per fantapunti non è quello usato dall'endpoint.
- Le regole complete, le forme dei DTO e le differenze tra documentazione OpenAPI e handler sono in `CONTESTO_COMPLETO.md`.
