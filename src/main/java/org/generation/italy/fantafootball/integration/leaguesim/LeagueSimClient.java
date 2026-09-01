package org.generation.italy.fantafootball.integration.leaguesim;

import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimMatchdayDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerDto;
import org.generation.italy.fantafootball.integration.leaguesim.dto.LeagueSimPlayerResultDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

// Questa classe e' l'unico punto del progetto che parla davvero via HTTP con LeagueSim.
// E' volutamente "muta": non contiene logica applicativa (niente salvataggi su DB, niente
// decisioni su cosa fare con i dati), solo la traduzione "chiamata REST -> oggetto Java".
// La logica di cosa farne (a chi associare i risultati, quando importarli, ecc.) vivra' in un
// service separato che usera' questa classe come dipendenza — cosi' se domani cambia il modo in
// cui interpretiamo i dati, non serve toccare il codice che parla con LeagueSim, e viceversa.
@Component
public class LeagueSimClient {

    private final RestClient restClient;

    // @Qualifier indica a Spring quale bean RestClient iniettare: nel progetto oggi ce n'e' uno
    // solo (leagueSimRestClient, definito in LeagueSimIntegrationConfig), ma essere espliciti
    // evita ambiguita' se in futuro venisse aggiunto un altro RestClient per un altro scopo.
    public LeagueSimClient(@Qualifier("leagueSimRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    // GET /api/players — l'intero catalogo giocatori conosciuto da LeagueSim.
    public List<LeagueSimPlayerDto> fetchPlayers() {
        return restClient.get()
                .uri("/api/players")
                .retrieve()
                // Una List<T> a runtime perde il tipo generico (type erasure): senza questo
                // ParameterizedTypeReference, Jackson non saprebbe che deserializzare in una
                // lista di LeagueSimPlayerDto e non, ad esempio, in una lista di LinkedHashMap.
                .body(new ParameterizedTypeReference<List<LeagueSimPlayerDto>>() {});
    }

    // GET /api/matchdays — tutte le giornate, con il rispettivo stato closed/aperta.
    public List<LeagueSimMatchdayDto> fetchMatchdays() {
        return restClient.get()
                .uri("/api/matchdays")
                .retrieve()
                .body(new ParameterizedTypeReference<List<LeagueSimMatchdayDto>>() {});
    }

    // GET /api/matchdays/{number}/results — le statistiche per giocatore di UNA giornata.
    // Ha senso chiamarlo solo per giornate gia' chiuse (closed = true su LeagueSimMatchdayDto):
    // per una giornata non ancora giocata i risultati semplicemente non esistono.
    public List<LeagueSimPlayerResultDto> fetchResults(int matchdayNumber) {
        return restClient.get()
                .uri("/api/matchdays/{number}/results", matchdayNumber)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LeagueSimPlayerResultDto>>() {});
    }

    // Nota per il prossimo passo: qui non gestiamo ancora errori di rete/HTTP (es. LeagueSim giu',
    // 401 per chiave sbagliata, 404 per giornata inesistente). Di default RestClient lancia
    // un'eccezione (RestClientResponseException e sottoclassi) sugli status 4xx/5xx: per ora
    // lasciamo che risalga al chiamante. Decideremo come gestirla (log, retry, ecc.) quando
    // costruiremo il service di sincronizzazione che usera' questi metodi.
}
