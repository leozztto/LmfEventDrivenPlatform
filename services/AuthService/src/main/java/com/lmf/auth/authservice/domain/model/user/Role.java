package com.lmf.auth.authservice.domain.model.user;

/**
 * Papéis atribuíveis a um usuário. O nome já inclui o prefixo {@code ROLE_} para casar diretamente
 * com as authorities do Spring Security (tanto na emissão do JWT quanto na validação no Gateway).
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
