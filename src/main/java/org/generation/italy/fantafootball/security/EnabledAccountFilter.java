package org.generation.italy.fantafootball.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class EnabledAccountFilter extends OncePerRequestFilter {
    private final AppUserRepository appUserRepository;

    public EnabledAccountFilter(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Jwt jwt = jwtAuthentication.getToken();
            Object userIdClaim = jwt.getClaim("uid");
            Object tokenVersionClaim = jwt.getClaim("tokenVersion");
            boolean enabled = userIdClaim instanceof Number userId
                    && tokenVersionClaim instanceof Number tokenVersion
                    && appUserRepository.findById(userId.longValue())
                        .map(user -> user.isEnabled()
                                && user.getTokenVersion() == tokenVersion.intValue()
                                && user.getUsername().equals(jwt.getSubject()))
                        .orElse(false);

            if (!enabled) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Account disabilitato o non disponibile");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
