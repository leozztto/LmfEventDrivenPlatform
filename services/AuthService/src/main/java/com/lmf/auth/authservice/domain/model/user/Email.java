package com.lmf.auth.authservice.domain.model.user;

import com.lmf.auth.authservice.domain.exception.InvalidEmailException;

import java.util.regex.Pattern;

/**
 * Value object de e-mail. Normaliza para minúsculas e valida um formato básico na construção.
 */
public record Email(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("E-mail é obrigatório");
        }
        value = value.trim().toLowerCase();
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("E-mail inválido: " + value);
        }
    }
}
