package com.lmf.auth.authservice.infrastructure.web.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "usernameOrEmail é obrigatório")
        String usernameOrEmail,

        @NotBlank(message = "password é obrigatório")
        String password) {
}
