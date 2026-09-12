# Paginazione dei giocatori

> Aggiornato il 12 settembre 2026. Riferimento generale: [CONTESTO_COMPLETO.md](../CONTESTO_COMPLETO.md), sezioni 6 e 8. Questo file approfondisce il funzionamento della paginazione.

Entrambi gli endpoint restituiscono ora una pagina, anche senza parametri:

```http
GET /api/players?page=0&size=20
GET /api/leagues/3/players/available?page=0&size=20
```

`page` parte da 0 (default 0). `size` indica il massimo di giocatori per pagina
(default 20, valori ammessi 1–100). L'ordinamento e' per ruolo (P, D, C, A),
cognome, nome e infine ID crescente, applicato nel database prima della pagina.
Parametri negativi, dimensioni fuori intervallo, valori non numerici o un offset
oltre il limite intero supportato da JPA restituiscono 400.
Una pagina oltre l'ultima restituisce 200 con `content: []` e i conteggi reali.

## Segui una richiesta nel codice

Prova `GET /api/players?page=1&size=20`: stai chiedendo la seconda pagina.

1. `PlayerController.getPlayers` legge i parametri con `@RequestParam` e applica
   i valori predefiniti quando mancano. I filtri esistenti restano in
   `PlayerFilterRequest`.
2. `PlayerService.findPlayers` costruisce le condizioni di ricerca e chiama
   `PlayerPagination.of(page, size)`, che valida i numeri e crea un `PageRequest`.
   `PageRequest` implementa `Pageable`: descrive pagina, dimensione e ordinamento.
3. `playerRepository.findAll(specification, pageable)` passa filtri e paginazione
   a JPA. Il database applica l'ordinamento per ruolo, cognome, nome e ID e
   l'equivalente di `LIMIT 20 OFFSET 20`:
   l'offset e' `page * size`. Le entita' restituite sono solo quelle della pagina.
4. Spring Data restituisce una `Page<Player>`: i risultati e i metadati.
   Quando necessario esegue anche una query `COUNT` per calcolare il totale
   dei giocatori che soddisfano i filtri, senza caricarli tutti come entita'.
5. `.map(PlayerResponse::fromEntity)` converte i giocatori della pagina in DTO,
   conservando i metadati. `PageResponse.fromPage` definisce il JSON pubblico.

Per la lega il percorso passa da `LeagueController` e `LeagueService`:
prima si verificano esistenza e accesso (404/403), poi il repository usa
`NOT EXISTS` per escludere i possessi attivi in quella lega. La paginazione e
il conteggio si applicano ai soli disponibili. I filtri del catalogo sono
supportati da `/api/players`; l'endpoint della lega supporta invece `search`
oltre a `page` e `size`, senza gli altri filtri del catalogo. Su entrambi,
`search` cerca nome, cognome o nome completo senza distinzione tra maiuscole
e minuscole e tratta letteralmente `%`, `_` e `!`.

Nel catalogo `role` e `realTeamName` accettano piu' valori; sono presenti anche
`injured`, `minPrice` e `maxPrice`. Gli endpoint `/api/players/real-teams` e
`/api/players/price-range` forniscono i dati dei filtri: il secondo considera
i filtri non di prezzo e ignora `minPrice`/`maxPrice` nel calcolo degli estremi.

## Risposta e frontend

Esempio con un solo giocatore corrispondente alla ricerca:

```json
{
  "content": [
    {
      "id": 12,
      "externalId": 101,
      "name": "Mario",
      "surname": "Rossi",
      "role": "A",
      "realTeamName": "Roma",
      "realTeamShirtNum": 9,
      "price": 20,
      "injured": false
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

Il frontend deve leggere `response.content` al posto del precedente array alla
radice. `size` e' il limite richiesto, mentre `content.length` e' il numero
effettivamente ricevuto. `totalElements` conta tutti i risultati della ricerca,
non solo quelli della pagina. `hasNext` indica se abilitare il pulsante Avanti.

Esempio JavaScript da adattare al frontend, passando il token della sessione:

```javascript
async function caricaPagina(token, page = 0) {
  const params = new URLSearchParams({ page: String(page), size: "20" });
  const response = await fetch(`/api/players?${params}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!response.ok) throw new Error(`Richiesta fallita: ${response.status}`);
  return response.json();
}

const prima = await caricaPagina(token, 0);
// Mostra prima.content, disabilita Indietro se prima.page === 0.
// Al click su Avanti, se prima.hasNext:
// const seconda = await caricaPagina(token, prima.page + 1);
```

Quando cambi filtri, riparti da pagina 0. Dopo un acquisto ricarica i disponibili,
preferibilmente dalla prima pagina: la lista e il totale possono essersi ridotti.
L'ordinamento e' stabile a dati invariati, ma le pagine non rappresentano una
fotografia immutabile: acquisti o inserimenti fra richieste possono spostare i
risultati. Per milioni di record, conteggi e offset molto profondi possono essere
costosi; una paginazione a cursore sarebbe un'evoluzione da valutare con misure reali.

## Piccolo esercizio

Con almeno due giocatori disponibili, chiama lo stesso endpoint con `size=1`:

1. `page=0`: osserva il primo ID, `totalElements` e `hasNext`.
2. `page=1`: l'ID cambia, il totale resta uguale se i dati non cambiano.
3. Acquista il primo giocatore nella lega e ricarica `players/available?page=0&size=1`:
   il giocatore acquistato scompare e il totale diminuisce.
4. Prova `size=101`: ricevi 400 per impedire richieste troppo grandi.

Questi comportamenti sono verificati anche dai test HTTP con database H2 in
`AvailablePlayersIntegrationTest`; `PlayerServiceTest` verifica il passaggio
della paginazione al repository e la conversione delle risposte.
