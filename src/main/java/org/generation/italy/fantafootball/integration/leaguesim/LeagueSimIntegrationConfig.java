package org.generation.italy.fantafootball.integration.leaguesim;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

// Questa classe configura come fantafootball parla con il servizio esterno LeagueSim via HTTP.
// @EnableConfigurationProperties registra LeagueSimProperties come bean: cosi' Spring legge
// leaguesim.base-url / leaguesim.api-key / ... da application.properties (e dalle env var che le
// valorizzano) e li inietta gia' pronti dentro LeagueSimProperties, senza doverli leggere a mano.
//
// @EnableScheduling accende il meccanismo di scheduling di Spring (senza, le @Scheduled su
// LeagueSimScheduler verrebbero ignorate). E' un'impostazione a livello di intera applicazione,
// la mettiamo qui perche' oggi e' la sincronizzazione con LeagueSim l'unica cosa che la usa.
@Configuration
@EnableConfigurationProperties(LeagueSimProperties.class)
@EnableScheduling
public class LeagueSimIntegrationConfig {

    // RestClient e' il client HTTP sincrono di Spring (dalla versione 6.1 in poi) usato per
    // chiamare altri servizi via REST. E' sincrono/bloccante come il resto della nostra app
    // (che usa Spring MVC, non reattivo), quindi qui non serve WebClient (pensato per stack reattivi).
    //
    // RestClient.Builder e' un bean che Spring Boot fornisce gia' pronto (autoconfigurazione):
    // lo prendiamo come parametro e lo usiamo per costruire UN client specifico per LeagueSim,
    // con impostazioni fisse che valgono per ogni chiamata fatta con questo bean:
    //
    // - baseUrl: l'indirizzo base di LeagueSim (es. http://localhost:8080); nelle chiamate
    //   basterà indicare il path (es. "/api/players") e non l'URL completo ogni volta.
    // - defaultHeader("X-API-KEY", ...): aggiunge automaticamente l'header di autenticazione
    //   richiesto da LeagueSim a OGNI richiesta fatta con questo client, cosi' non c'e' rischio
    //   di dimenticarlo (o di scriverlo a mano in giro nel codice) quando implementeremo le
    //   chiamate vere e proprie.
    // - requestFactory: vedi sotto, imposta i timeout.
    @Bean
    public RestClient leagueSimRestClient(RestClient.Builder builder, LeagueSimProperties properties) {
        return builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("X-API-KEY", properties.apiKey())
                .requestFactory(leagueSimRequestFactory(properties))
                .build();
    }

    // Un timeout dice quanto aspettare al massimo prima di considerare la chiamata fallita.
    // Senza timeout, se LeagueSim fosse lento o irraggiungibile, la richiesta potrebbe restare
    // in attesa a tempo indeterminato, bloccando un thread del nostro server (che e' sincrono).
    // - connectTimeout: quanto aspettare per stabilire la connessione TCP con LeagueSim.
    // - readTimeout: quanto aspettare per ricevere la risposta una volta connessi.
    // I valori arrivano da LeagueSimProperties (configurabili via property/env var, con default
    // ragionevoli se non specificati: vedi il compact constructor di LeagueSimProperties).
    private ClientHttpRequestFactory leagueSimRequestFactory(LeagueSimProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());
        return factory;
    }
}
