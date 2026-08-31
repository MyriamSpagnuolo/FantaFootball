package org.generation.italy.fantafootball.security;

import org.generation.italy.fantafootball.model.entities.AppUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final AppJwtProperties props;

    public JwtService(JwtEncoder jwtEncoder, AppJwtProperties props) {
        this.jwtEncoder = jwtEncoder;
        this.props = props;
    }

    public String createToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(props.ttl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("tokenVersion", user.getTokenVersion())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .build();

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }
}
