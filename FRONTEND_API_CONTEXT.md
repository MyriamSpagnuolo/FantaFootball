# FantaFootball — contesto API per sviluppo frontend

> Questo file è pensato per essere letto da un'altra sessione/agente che deve
> pianificare o sviluppare il frontend di FantaFootball, senza dover rileggere
> tutto il backend. Riflette lo stato del codice al momento della stesura —
> se il backend cambia, va rigenerato (non è un contratto API mantenuto a mano
> nel tempo, è uno snapshot di lavoro).

## Come gira il backend

- Spring Boot, porta di default **8081**, base path `/api`.
- Autenticazione **JWT stateless** (Bearer token), niente sessioni/cookie.
- Swagger UI disponibile su `http://localhost:8081/swagger-ui.html` (spec grezza su `/v3/api-docs`) — ma i controller **non hanno annotazioni OpenAPI** (`@Operation`/`@Tag`/`@ApiResponse`), quindi lì si vede solo la forma delle request/response, non le regole di business descritte qui sotto.

## Autenticazione

- `POST /api/auth/login` → `{token, roles[]}`. Da qui in poi ogni chiamata (tranne quelle pubbliche sotto) richiede header `Authorization: Bearer <token>`.
- Il backend estrae l'id utente dal claim JWT `uid` (non dallo username) — è l'id da tenere per costruire richieste che coinvolgono "l'utente corrente".
- `POST /api/auth/logout` esiste ma non fa nulla lato server (stateless): il frontend deve solo scartare il token.
- **Endpoint pubblici (nessun token richiesto)**: `POST /api/auth/login`, `/register`, `/forgot-password`, `/reset-password`.
- Attenzione: `SecurityConfig` marca come pubblici anche `/api/public/**` e `POST /api/registration-requests`, ma **nessun controller li implementa** — non pianificare feature frontend su questi path, sono config orfane.

## Formato errori

Tutte le eccezioni di business rispondono con body `{"errorCode": "...", "message": "..."}` e lo status HTTP coerente (400/404/409/403). Da tenere presente per la UI:

- **Il casing di `errorCode` non è uniforme tra service**: `TradeService`/`AccountService`/`AuthService`/`LeagueMatchService`/`ExternalAuctionImportService` usano `snake_case` minuscolo (es. `"user_not_found"`), mentre `TeamService`/`LeagueInviteService` usano `SCREAMING_SNAKE_CASE` (es. `"USER_NOT_FOUND"`). Non affidarti a un case fisso, fai match esatto sulla stringa documentata per endpoint.
- 403 (`errorCode: "access_denied"`) è usato sia per problemi di autenticazione (token senza claim `uid`) sia per violazioni di ownership/ruolo lega (es. "non sei l'admin di questa lega") — il `message` è l'unico modo per distinguerli in UI.
- 401 vero e proprio (token mancante/invalido/scaduto) non passa dal `GlobalExceptionHandler`: lo gestisce il resource server di Spring Security prima di arrivare al controller.

## Modello di dominio essenziale

- **AppUser** possiede una **Team** per **League** al massimo (vincolo enforced lato service, non solo DB).
- **League**: ha un `admin` (un `AppUser`), un `inviteCode` (generato ma — vedi sotto — **non usato da nessun endpoint per un self-join**), un budget iniziale ereditato da ogni `Team` creata.
- **Team**: appartiene a una `League` e a uno `AppUser`; ha un `budget` che scende con acquisti/scambi, e un roster di `TeamPlayer`.
- **TeamPlayer**: un `Player` posseduto da una `Team` in una `League`, con `purchaseDate`/`purchasePrice`/`transferDate` (`transferDate` valorizzato = il giocatore è stato ceduto/svincolato, non più "attivo" in quella squadra).
- **LeagueInvite**: invito nominale (per username) da parte dell'admin di lega verso uno user. **È l'unico modo per un non-admin di entrare in una lega** — l'`inviteCode` sulla `League` non è collegato a nessun flusso di join.
- **Trade**: scambio `PENDING` tra due `Team` (giocatore-per-giocatore + eventuale conguaglio in `amount`), poi `ACCEPTED`/`REJECTED`/`CANCELLED`.
- **Matchday**: giornata "reale" sincronizzata da LeagueSim (number, date, `closed`) — non creabile/modificabile da FantaFootball, arriva via polling automatico (vedi `CLAUDE.md`).
- **LeagueMatch**: partita fantacalcistica generata dal calendario round-robin di una `League` per un `Matchday`.
- **Lineup / LineupPlayer**: la formazione schierata da una `Team` per un `LeagueMatch`. **Nessun controller espone la creazione/modifica di una Lineup** — esiste solo la lettura del punteggio di una lineup già esistente (`GET /api/lineups/{id}/score`). Se il frontend deve permettere di schierare una formazione, quell'endpoint di scrittura **non esiste ancora** ed è probabilmente il gap più importante da colmare prima di costruire quella feature.
- **PlayerResult**: voto/statistiche reali di un `Player` per un `Matchday`, importato da LeagueSim, indipendente da qualunque `Lineup`.

## Endpoint per risorsa

### Auth — `/api/auth` (pubblici)

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| POST | `/login` | `LoginRequest{username, password}` | `LoginResponse{token, roles[]}` | |
| POST | `/register` | `CreateUserRequest{username, email, password}` | `UserDto{id, username, enabled, roles[]}` → 201 | `password` deve rispettare `@StrongPassword`; 409 `username_unavailable`/`email_unavailable` |
| POST | `/logout` | — | 204 | No-op lato server |
| POST | `/forgot-password` | `ForgotPasswordRequest{email}` | 204 | Sempre 204 anche se l'email non esiste (non rivela se l'utente esiste) |
| POST | `/reset-password` | `ResetPasswordRequest{token, newPassword}` | 204 | 400 `invalid_reset_token` se scaduto/usato/utente disabilitato |

### Account — `/api/account` (autenticato, sempre "utente corrente")

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| GET | `/me/leagues` | — | `List<UserLeagueTeamResponse>` (`{league, team, admin}`) | Tutte le leghe/team dell'utente, con flag se ne è admin |
| PATCH | `/me/username` | `UpdateUsernameRequest{newUsername, currentPassword}` | 204 | Richiede password corrente; invalida i JWT esistenti (`tokenVersion++`) |
| PUT | `/me/password` | `ChangePasswordRequest{currentPassword, newPassword}` | 204 | Invalida i JWT esistenti |
| DELETE | `/me` | `DisableAccountRequest{currentPassword}` | 204 | Disabilita l'account (soft, `enabled=false`), non lo cancella |

### Leagues — `/api/leagues`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| POST | `/leagues` | `CreateLeagueRequest{name, teamName, budget}` | `LeagueResponse` → 201 | Crea la lega **e** la prima `Team` (dell'admin) in un'unica chiamata |
| GET | `/leagues/{leagueId}/teams` | — | `List<TeamStandingResponse>` (`{teamId, teamName, username, budget, totalPoints}`) | 403 se l'utente non ha una `Team` in quella lega |

### League invites — invio: `/api/leagues/{leagueId}/invites`, risposta: `/api/invites`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| POST | `/api/leagues/{leagueId}/invites` | `CreateInviteRequest{invitedUsername}` | `InviteResponse` → 201 | Solo admin di lega (409 `NOT_LEAGUE_ADMIN` altrimenti); 409 se invito pending duplicato o utente già in lega |
| GET | `/api/invites/pending` | — | `List<InviteResponse>` | Inviti pending per l'utente corrente |
| PATCH | `/api/invites/{inviteId}` | `UpdateInviteStatusRequest{status: ACCEPTED\|DECLINED}` | `InviteResponse` | Solo il destinatario; 409 se non pending; **accettare l'invito NON crea la Team** — dopo serve una `POST /api/teams` separata |

`InviteResponse`: `{id, leagueId, invitedByUserId, invitedUserId, status, sentDate, responseDate}`. `LeagueInviteStatus`: `PENDING | ACCEPTED | DECLINED | EXPIRED` (`EXPIRED` esiste nell'enum ma non risulta mai impostato da nessun service).

### Teams — `/api/teams`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| POST | `/teams` | `CreateTeamRequest{teamName, leagueId}` | `TeamResponse` → 201 | Se non sei admin della lega, richiede un invito `ACCEPTED` (409 `INVITE_PENDING` se ancora da accettare, 403 se nessun invito/non accettato) |
| GET | `/teams/me` | — | `List<TeamResponse>` | Tutte le squadre dell'utente corrente |
| PATCH | `/teams/{teamId}` | `RenameTeamRequest{name}` | `TeamResponse` | Solo il proprietario (403 altrimenti) |
| GET | `/teams/{teamId}/players` | — | `List<TeamPlayerResponse>` | Proprietario **o** admin di lega |
| DELETE | `/teams/{teamId}/players/{playerId}` | — | 204 | Proprietario **o** admin di lega; 409 `PLAYER_IN_USE` se il giocatore è schierato in almeno una lineup |

`TeamResponse`: `{id, name, userId, leagueId, leagueName, budget, totalPoints}`. `TeamPlayerResponse`: `{id, teamId, playerId, name, surname, realTeamName, realTeamShirtNum, injured, purchaseDate, transferDate, purchasePrice}`.

### League matches (calendario) — `/api/leagues/{leagueId}/matches`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| POST | `/api/leagues/{leagueId}/matches` | — | `List<LeagueMatchDto>` → 201 | Solo admin di lega; genera il calendario round-robin **una sola volta** (409 `calendar_already_generated` se richiamato di nuovo); fallisce se non ci sono `Matchday` aperte disponibili da LeagueSim |

`LeagueMatchDto`: `{id, roundNumber, matchDay, homeTeamId, homeTeamName, awayTeamId, awayTeamName}`. Non c'è un `GET` per rileggere il calendario già generato — solo la risposta del `POST`.

### Trades — `POST /api/trades`, `GET /api/trades`, `GET /api/teams/{teamId}/trades`, `PATCH /api/trades/{id}`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| POST | `/api/trades` | `CreateTradeRequest{receivingTeamId, requestedPlayerId, offeredPlayerId, amount?}` | `TradeDto` → 201 | `amount` positivo = lo paga chi propone, negativo = lo paga chi riceve; entrambe le squadre devono essere nella stessa lega |
| GET | `/api/trades` | — | `List<TradeDto>` | Tutti gli scambi (proposti o ricevuti) dell'utente corrente |
| GET | `/api/teams/{teamId}/trades?scope=history` | — | `List<TradeDto>` | Storico completo per la squadra |
| GET | `/api/teams/{teamId}/trades?status=pending&direction=received` | — | `List<TradeDto>` | Scambi pending ricevuti |
| GET | `/api/teams/{teamId}/trades?status=pending&direction=sent` | — | `List<TradeDto>` | Scambi pending inviati |
| PATCH | `/api/trades/{id}` | `UpdateTradeStatusRequest{status: ACCEPTED\|REJECTED}` | 204 | Accettare: solo il destinatario, verifica budget e disponibilità giocatori, **cancella automaticamente altri trade pending che coinvolgono gli stessi due giocatori**; Rifiutare: uno dei due partecipanti |

`TradeDto`: `{id, proposingTeamId, proposingTeamName, receivingTeamId, receivingTeamName, requestedPlayerName, offeredPlayerName, amount, status, proposalDate}`. `TradeStatus`: `PENDING | ACCEPTED | REJECTED | CANCELLED`. Combinazioni di query param diverse da quelle in tabella → 400 `invalid_trade_filters`.

### Players — `/api/players`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| GET | `/players?role=&realTeamName=&minPrice=&maxPrice=&injured=` | query params opzionali | `List<PlayerResponse>` | Catalogo completo dei giocatori reali (da LeagueSim), non filtrato per lega/team |
| GET | `/players/{playerId}/matchdays/{matchdayId}/rating` | — | `PlayerRatingResponse{fantaRating: double}` | Fantavoto del giocatore per quella giornata, **indipendente da ogni lineup**; 404 `player_result_not_found` se non ha un `PlayerResult` per quella giornata (es. non ha giocato) |

`PlayerResponse`: `{id, externalId, name, surname, role, realTeamName, realTeamShirtNum, price, injured}`. `role` è `PlayerRole`: `P | D | C | A`.

### Formazioni / punteggio — `/api/lineups`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| GET | `/api/lineups/{lineupId}/score` | — | `{score: double, goals: int}` | Punteggio fantacalcistico aggregato della formazione (titolari + sostituzioni per ruolo + modificatore difesa); 404 `lineup_not_found`, 409 `matchday_not_closed` se la giornata non è ancora chiusa, 409 `lineup_team_mismatch` se dati incoerenti |

**Non esiste alcun endpoint per creare/modificare una `Lineup`** (schierare titolari/panchina) — vedi nota nel modello di dominio sopra.

### Asta esterna (acquisto giocatori) — `/api/leagues/{leagueId}/teams/{teamId}/players/{playerId}`

| Metodo | Path | Request | Response | Note |
|---|---|---|---|---|
| POST | `.../players/{playerId}` | `PurchasePlayerRequest{purchasePrice}` | 204 | Solo admin di lega (non il proprietario della squadra!); 409 `player_already_owned`/`budget_too_low` |

Da notare: chi esegue l'acquisto è sempre l'**admin di lega**, non il proprietario del team destinatario — probabile flusso "asta gestita manualmente dall'admin", non un acquisto self-service.

## Riepilogo autorizzazioni per ruolo

| Azione | Chi può farla |
|---|---|
| Generare calendario lega, inviare inviti, importare giocatori da asta | Solo `League.admin` |
| Rinominare team, vedere/gestire il proprio roster | Proprietario del team |
| Vedere/svincolare giocatori di un roster | Proprietario **o** admin di lega |
| Creare una Team in una lega (non da admin) | Richiede `LeagueInvite` con status `ACCEPTED` |
| Accettare/rifiutare un trade | Solo i due team coinvolti (ricevente per accettare, entrambi per rifiutare) |

Il ruolo globale `AppUser.roles` (`ADMIN`/`USER`) **non c'entra** con nessuna di queste regole — è usato solo per il bootstrap dell'utente HEAD interno (vedi `CLAUDE.md`).

## Cosa manca / punti da chiarire per il frontend

- **Nessuna scrittura per le Lineup**: se il frontend deve permettere di schierare una formazione, l'endpoint non esiste — va progettato e implementato lato backend prima.
- **Nessun self-join tramite `inviteCode`**: l'unico modo per un utente di entrare in una lega è essere invitato per username dall'admin. Se serve un "entra con codice", va costruito da zero.
- **Nessun `GET` per rileggere il calendario** dopo la generazione (`LeagueMatchController` ha solo il `POST`) — probabile endpoint mancante se il frontend deve mostrare il calendario in una pagina separata dal momento della generazione.
- **`errorCode` non uniforme nel case** tra i vari service (vedi sopra) — se il frontend vuole mappare errorCode → messaggi localizzati/UI specifica, va fatto per stringa esatta, non per convenzione di case.
- Swagger UI esiste ma senza annotazioni descrittive: utile solo per la forma di request/response, non per le regole di business — usa questo documento per quelle.
