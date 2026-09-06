package com.lmf.auth.authservice.infrastructure.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username é obrigatório")
        @Size(max = 100, message = "username deve ter no máximo 100 caracteres")
        String username,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        String email,

        @NotBlank(message = "password é obrigatório")
        @Size(min = 8, max = 100, message = "password deve ter entre 8 e 100 caracteres")
        String password) {
}
