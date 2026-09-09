package org.generation.italy.fantafootball.integration.leaguesim;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "leaguesim")
public record LeagueSimProperties(
        String baseUrl,
        String apiKey,
        Duration connectTimeout,
        Duration readTimeout
) {
    public LeagueSimProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "LeagueSim base URL is not configured. Set leaguesim.base-url (e.g. via LEAGUESIM_BASE_URL env var)");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "LeagueSim API key is not configured. Set leaguesim.api-key (e.g. via LEAGUESIM_API_KEY env var)");
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(3);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(5);
        }
    }
}
