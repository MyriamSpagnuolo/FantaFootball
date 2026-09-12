# FantaFootball — contesto per il frontend

Aggiornato il **12 settembre 2026**. Il contratto verificato e completo è ora raccolto in [CONTESTO_COMPLETO.md](CONTESTO_COMPLETO.md), insieme a dominio, autorizzazioni e regole di business. Questo file è il punto di ingresso per chi integra il frontend e sostituisce il precedente snapshot, che riportava come mancanti funzionalità già implementate.

Nel documento principale consultare:

| Sezione | Contenuto |
|---|---|
| 4 | Entità e differenze tra ID di giocatori, possessi, giornate e partite. |
| 5 | Login, invalidazione JWT, recupero password e autorizzazioni effettive. |
| 6 | Flussi di lega, inviti, catalogo, acquisti, scambi, calendario e formazioni. |
| 7 | Fantavoti, sostituzioni, modificatore difesa, risultati e classifica. |
| 8 | Tutti gli endpoint correnti, parametri e campi dei DTO. |
| 9 | Errori e differenze tra OpenAPI e risposte effettive. |
| 12 | URL locali, CORS, configurazione e avvio. |

## Sequenza di integrazione

1. Registrazione e login; usare il token Bearer. I quattro POST pubblici sono `/api/auth/login`, `/api/auth/register`, `/api/auth/forgot-password`, `/api/auth/reset-password`.
2. Caricare `/api/account/me/leagues` per mostrare squadre, leghe e flag admin dell'utente.
3. Creare una lega con la prima squadra oppure accettare un invito e poi creare la squadra con una richiesta separata.
4. Caricare catalogo e disponibili leggendo `response.content`: sono pagine, non array alla radice.
5. Registrare gli acquisti come admin e gestire gli scambi con gli ID corretti dei possessi.
6. Generare il calendario come admin e rileggerlo con il GET della stessa risorsa.
7. Leggere i moduli e salvare la formazione con POST/PUT, usando gli ID `TeamPlayer` attivi.
8. Mostrare voti, risultati e classifica tenendo distinti assenza di risultato, voto nullo e punteggio zero.

## Correzioni essenziali rispetto al vecchio snapshot

- GET/POST/PUT `/api/teams/{teamId}/matches/{leagueMatchId}/lineup` esistono; GET `/api/lineup-types` restituisce i moduli.
- GET `/api/leagues/{leagueId}/matches` esiste e include fantapunti, gol e `matchdayClosed`.
- GET `/api/leagues/{leagueId}`, GET `/api/leagues/{leagueId}/trades` e GET `/api/teams/{teamId}/matches/{leagueMatchId}/players/ratings` sono disponibili.
- Catalogo e disponibili usano `page=0`, `size=20` di default, massimo 100; ordinamento P/D/C/A, cognome, nome e ID. La ricerca `search` è disponibile su entrambi; gli altri filtri del catalogo non sono filtri dei disponibili.
- `TeamPlayerResponse.id` è l'ID da usare per formazione, scambi e svincolo. L'acquisto usa invece `Player.id`.
- La lettura rosa è accessibile a qualsiasi utente autenticato e include lo storico: filtrare `transferDate == null` per i giocatori attivi. Il ruolo si chiama `playerRole` in questa risposta.
- L'accettazione dell'invito non crea la squadra. Il codice invito non ha un endpoint di self-join.
- Cambio username/password e reset invalidano i JWT precedenti. Logout richiede autenticazione ed elimina il token solo lato client.
- I codici errore hanno casing e alcuni status non uniformi: consultare la sezione 9 invece di dedurre una convenzione.

URL predefinito backend: `http://localhost:8081`. Swagger: `http://localhost:8081/swagger-ui.html`. Per i dettagli della paginazione resta disponibile [docs/paginazione-giocatori.md](docs/paginazione-giocatori.md).
