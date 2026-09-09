package org.generation.italy.fantafootball.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.password-reset")
public record AppPasswordResetProperties(Duration ttl, String frontendUrl, String from) {}
