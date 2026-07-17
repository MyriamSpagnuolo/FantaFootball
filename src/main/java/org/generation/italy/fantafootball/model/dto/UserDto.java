package org.generation.italy.fantafootball.model.dto;

import java.util.Set;

public record UserDto(
        long id,
        String username,
        boolean enabled,
        Set<String> roles
) {}

