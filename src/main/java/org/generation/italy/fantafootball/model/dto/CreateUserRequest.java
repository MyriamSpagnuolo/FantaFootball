package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.generation.italy.fantafootball.model.validation.StrongPassword;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 80)
        String username,

        @NotBlank
        @StrongPassword
        String password

) {}
