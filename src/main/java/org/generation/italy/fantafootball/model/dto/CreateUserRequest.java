package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.generation.italy.fantafootball.model.validation.StrongPassword;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 80)
        String username,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @StrongPassword
        String password

) {}
