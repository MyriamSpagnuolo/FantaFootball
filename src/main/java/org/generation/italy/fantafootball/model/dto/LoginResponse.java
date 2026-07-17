package org.generation.italy.fantafootball.model.dto;

import java.util.List;

public record LoginResponse(
        String token,
        List<String> roles
) {}

